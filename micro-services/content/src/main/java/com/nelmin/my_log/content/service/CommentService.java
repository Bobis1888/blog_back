package com.nelmin.my_log.content.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nelmin.my_log.content.dto.comment.*;
import com.nelmin.my_log.content.dto.kafka.ContentEvent;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.model.Comment;
import com.nelmin.my_log.content.model.Vote;
import com.nelmin.my_log.user_info.core.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserInfo userInfo;
    private final Comment.Repo commentRepo;
    private final Article.Repo contentRepo;
    private final Vote.Repo voteRepo;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CommonHttpClient commonHttpClient;

    @Value("${content.events.topic:content-events}")
    private String eventsTopic;

    @Transactional
    public CreateCommentResponseDto save(CreateCommentRequestDto dto) {
        var response = new CreateCommentResponseDto();

        try {
            var comment = new Comment();
            comment.setUserId(userInfo.getId());
            comment.setContent(dto.comment());
            comment.setArticleId(dto.contentId());

            if (dto.parentId() != null) {
                commentRepo.findById(dto.parentId()).ifPresent(comment::setParent);
            }

            comment = commentRepo.save(comment);
            response.setSuccess(true);

            // TODO
            if (!contentRepo.existsByUserIdAndId(userInfo.getId(), comment.getArticleId())) {
                sendEvent(comment, ContentEvent.Type.COMMENT);
            }

            // TODO
            if (comment.getParent() != null) {
                sendEvent(comment, ContentEvent.Type.REPLY_COMMENT);
            }
        } catch (Exception ex) {
            log.error("Error save comment", ex);
            response.reject("serverError");
        }

        return response;
    }

    @Transactional
    public void vote(Long commentId, Boolean value) {

        try {

            if (!commentRepo.existsById(commentId)) {
                log.error("Comment not found {}", commentId);
                return;
            }

            var vote = voteRepo.findByCommentId(commentId).orElse(new Vote());
            vote.setCommentId(commentId);
            vote.setVote(value);
            vote.setUserId(userInfo.getId());
            voteRepo.save(vote);
        } catch (Exception ex) {
            log.error("Error vote comment", ex);
        }
    }

    @Transactional
    public void delete(Long commentId) {

        try {
            commentRepo.deleteByIdAndUserId(commentId, userInfo.getId());
        } catch (Exception ex) {
            log.error("Error vote comment", ex);
        }
    }

    @Transactional
    public ListCommentResponseDto list(ListCommentRequestDto dto) {
        var res = new ListCommentResponseDto();
        var direction = Optional.ofNullable(dto.direction()).orElse(Sort.Direction.DESC);

        try {
            var pageRequest = PageRequest.of(dto.page(), dto.max(), Sort.by(direction, "id"));
            var page = commentRepo.findAllByArticleId(dto.contentId(), pageRequest);

            if (!page.isEmpty()) {

                String listIds = page.stream().map(it -> {
                    var longs = new ArrayList<Long>();
                    longs.add(it.getUserId());
                    Optional.ofNullable(it.getParent())
                            .ifPresent(parent -> longs.add(parent.getUserId()));
                    return longs;
                }).flatMap(List::stream)
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));

                var response = resolveNicknames(listIds);

                res.setList(page.stream().map(it -> {
                    var commentDto = new CommentDto();
                    commentDto.setId(it.getId());
                    commentDto.setDate(it.getCreatedDate());
                    commentDto.setUserId(it.getUserId());
                    response.ifPresent(ids -> commentDto.setNickname(String.valueOf(ids.get(it.getUserId().toString()))));
                    commentDto.setContent(it.getContent());
                    commentDto.setRating(it.getRating());

                    var actions = new CommentDto.Actions();
                    actions.setCanDelete(it.getUserId().equals(userInfo.getId()));
                    actions.setCanVote(!voteRepo.existsByCommentIdAndUserId(it.getId(), userInfo.getId()));
                    actions.setCanReply(!Objects.equals(it.getUserId(), userInfo.getId()));
                    actions.setCanReport(!Objects.equals(it.getUserId(), userInfo.getId()));
                    commentDto.setActions(actions);

                    if (it.getParent() != null) {
                        var parent = new CommentDto.ParentDto();
                        parent.setContent(it.getParent().getContent());
                        response.ifPresent(ids -> parent.setNickname(String.valueOf(ids.get(it.getParent().getUserId().toString()))));
                        commentDto.setParent(parent);
                    }

                    return commentDto;
                }).toList());
                res.setTotalPages(page.getTotalPages());
                res.setTotalRows(page.getTotalElements());
            }
        } catch (Exception ex) {
            log.error("Error list comment", ex);
            res.reject("serverError");
        }

        return res;
    }

    private void sendEvent(Comment comment, ContentEvent.Type type) {
        String stringPayload;
        var payload = new HashMap<String, Object>();
        var response = resolveNicknames(comment.getUserId().toString());

        payload.put("userId", comment.getUserId());
        payload.put("articleTitle", contentRepo.getArticleTitleById(comment.getArticleId()));
        payload.put("articleId", comment.getArticleId().toString());
        response.ifPresent(it -> payload.put("username", it.get(comment.getUserId().toString())));

        try {
            stringPayload = objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            log.error("Error serialize comment payload", ex);
            return;
        }

        var event = new ContentEvent();
        event.setType(type);
        event.setPayload(stringPayload);

        if (type == ContentEvent.Type.COMMENT) {
            contentRepo.getUserIdById(comment.getArticleId()).ifPresent(userId -> event.setUserId(userId.getUserId()));
        }

        if (type == ContentEvent.Type.REPLY_COMMENT) {
            event.setUserId(comment.getParent().getUserId());
        }

        kafkaTemplate.send(eventsTopic, event).thenAccept(producerRecord -> log.info("Event sent {}", comment));
    }

    private Optional<Map> resolveNicknames(String ids) {
        return commonHttpClient.exchange("user/info/resolve_nicknames?ids=" + ids, HttpMethod.GET, Map.class);
    }
}
