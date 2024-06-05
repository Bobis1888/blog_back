package com.nelmin.blog.content.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.model.User;
import com.nelmin.blog.content.dto.*;
import com.nelmin.blog.content.model.Article;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.util.ClassUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final Article.Repo articleRepo;
    private final User.Repo userRepo;
    private final UserInfo userInfo;

    @Transactional
    public CreateContentResponseDto save(@NonNull CreateContentRequestDto dto) {
        log.info("create content: {}", dto);
        var response = new CreateContentResponseDto();
        Article article = null;

        if (dto.id() != null) {
            article = articleRepo
                    .findByIdAndUserId(dto.id(), userInfo.getCurrentUser()
                            .getId())
                    .orElseGet(Article::new);
        }

        if (article == null) {
            article = new Article();
        }

        article.setUserId(userInfo.getCurrentUser().getId());
        article.setContent(dto.content());
        article.setPreView(dto.preView());
        article.setTitle(dto.title());

        try {
            articleRepo.save(article);
            response.setSuccess(true);
            response.setId(article.getId());
        } catch (Exception ex) {
            log.error("Error save article", ex);
            response.setSuccess(false);
            response.reject("saveError", "article");
        }

        return response;
    }

    @Transactional
    public DeleteContentResponseDto delete(@NonNull Long id) {
        var response = new DeleteContentResponseDto();

        var article = articleRepo.findByIdAndUserId(id, userInfo.getCurrentUser().getId());

        if (article.isPresent()) {

            try {
                articleRepo.deleteById(id);
                response.setSuccess(true);
            } catch (Exception ex) {
                log.error("Error delete article", ex);
                response.setSuccess(false);
                response.reject("saveError", "article");
            }
        } else {
            response.setSuccess(false);
            response.reject("notFound", "article");
        }

        return response;
    }

    @Transactional
    public ArticleDto view(@NonNull Long id) {
        var res = new ArticleDto();
        var article = articleRepo.findById(id);

        if (article.isPresent() && (article.get().getStatus() == Article.Status.PUBLISHED ||
                (article.get().getStatus() == Article.Status.DRAFT &&
                        Objects.equals(article.get().getUserId(), userInfo.getCurrentUser().getId())))) {
            res.setId(article.get().getId());
            res.setContent(article.get().getContent());
            res.setTitle(article.get().getTitle());
        } else {
            res.reject("notFound", "article");
        }

        return res;
    }

    @Transactional
    public PublishContentResponseDto changeStatus(@NonNull Long id, @NonNull Article.Status status) {
        var response = new PublishContentResponseDto();
        var article = articleRepo.findByIdAndUserId(id, userInfo.getCurrentUser().getId());

        if (article.isPresent()) {

            try {
                article.get().setStatus(status);

                if (status == Article.Status.PUBLISHED) {
                    article.get().setPublishedDate(LocalDateTime.now());
                }

                articleRepo.save(article.get());

                response.setSuccess(true);
            } catch (Exception ex) {
                log.error("Error publish article", ex);
                response.setSuccess(false);
                response.reject("saveError", "article");
            }
        } else {
            response.reject("notFound", "article");
            response.setSuccess(false);
        }

        return response;
    }

    @Transactional
    public ListContentResponseDto list(ListContentRequestDto requestDto) {
        List<ArticleDto> resList = new ArrayList<>();
        List<Article.Status> statuses = new ArrayList<>();
        String[] sortBy = null;
        Long userId = null;

        if (requestDto.getSortBy() != null && !requestDto.getSortBy().isEmpty()) {
            sortBy = requestDto
                    .getSortBy()
                    .stream()
                    .filter(it -> ClassUtils.hasProperty(Article.class, it))
                    .toArray(String[]::new);
        }

        if (sortBy == null || sortBy.length == 0) {
            sortBy = new String[]{"id"};
        }

        if (requestDto.getUserId() == null || !Objects.equals(userInfo.getCurrentUser().getId(), requestDto.getUserId())) {
            statuses.add(Article.Status.PUBLISHED);
        } else if (Objects.equals(userInfo.getCurrentUser().getId(), requestDto.getUserId())) {
            statuses.add(Article.Status.DRAFT);
            statuses.add(Article.Status.PUBLISHED);
            userId = requestDto.getUserId();
        }

        var pageRequest = PageRequest.of(
                requestDto.getPage(),
                requestDto.getMax(),
                Sort.by(
                        requestDto.getDirection(),
                        sortBy
                )
        );

        // TODO
        Page<Article> page;

        if (userId != null) {
            page = articleRepo.findAllByStatusInAndUserId(statuses, userId, pageRequest);
        } else {
            page = articleRepo.findAllByStatusIn(statuses, pageRequest);
        }

        if (!page.isEmpty()) {
            resList.addAll(page
                    .getContent()
                    .stream()
                    .map(it -> new ArticleDto(
                            it.getId(),
                            it.getTitle(),
                            it.getPreView(),
                            it.getContent(),
                            it.getPublishedDate(),
                            resolveUserName(it.getUserId()),
                            it.getStatus().name().toLowerCase())
                    )
                    .toList()
            );
        }

        return new ListContentResponseDto(resList);
    }

    private String resolveUserName(Long id) {
        return userRepo.getNickNameById(id).orElseGet(() -> (User.NickName) () -> "unknown").getNickName();
    }
}
