package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.service.FillStatisticInfo;
import com.nelmin.my_log.common.service.UserService;
import com.nelmin.my_log.content.dto.StatisticsResponseDto;
import com.nelmin.my_log.content.dto.comment.*;
import com.nelmin.my_log.content.model.Comment;
import com.nelmin.my_log.content.model.Vote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService implements FillStatisticInfo<StatisticsResponseDto> {

    private final UserInfo userInfo;
    private final Comment.Repo commentRepo;
    private final UserService userService;
    private final Vote.Repo voteRepo;

    @Transactional
    public CreateCommentResponseDto save(CreateCommentRequestDto dto) {
        var response = new CreateCommentResponseDto();

        try {
            var comment = new Comment();
            comment.setUserId(userInfo.getId());
            comment.setContent(dto.comment());
            comment.setArticleId(dto.contentId());
            commentRepo.save(comment);
            response.setSuccess(true);
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
    public ListCommentResponseDto list(ListCommentRequestDto dto) {
        var res = new ListCommentResponseDto();

        try {
            var pageRequest = PageRequest.of(dto.page(), dto.max(), Sort.by("id").descending());
            var page = commentRepo.findAllByArticleId(dto.contentId(), pageRequest);

            if (!page.isEmpty()) {
                res.setList(
                        page.stream()
                                .map(it -> {
                                    var commentDto = new CommentDto();
                                    commentDto.setId(it.getId());
                                    commentDto.setDate(it.getCreatedDate());
                                    commentDto.setNickname(userService.resolveNickname(it.getUserId()));
                                    commentDto.setContent(it.getContent());
                                    commentDto.setRating(it.getRating());
                                    return commentDto;
                                })
                                .toList()
                );
                res.setTotalPages(page.getTotalPages());
                res.setTotalRows(page.getTotalElements());
            }
        } catch (Exception ex) {
            log.error("Error list comment", ex);
            res.reject("serverError");
        }

        return res;
    }

    @Override
    public void fillStatisticInfo(StatisticsResponseDto response) {
        response.setComments(commentRepo.countByUserId(response.getUserid()));
    }
}
