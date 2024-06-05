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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

        // TODO validate html/sql injection
        article.setContent(dto.content());
        article.setPreView(dto.preView());
        article.setTitle(dto.title());
        article.setTags(dto.tags());

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
    public ArticleDto get(@NonNull Long id) {
        var res = new ArticleDto();
        var article = articleRepo.findById(id);

        if (article.isPresent() && (article.get().getStatus() == Article.Status.PUBLISHED ||
                (article.get().getStatus() == Article.Status.DRAFT &&
                        Objects.equals(article.get().getUserId(), userInfo.getCurrentUser().getId())))) {
            res.setId(article.get().getId());
            res.setContent(article.get().getContent());
            res.setTitle(article.get().getTitle());
            res.setStatus(article.get().getStatus().name().toLowerCase());
            res.setPublishedDate(article.get().getPublishedDate());
            res.setAuthorName(resolveUserName(article.get().getUserId()));
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
        String query = null;
        String tags = null;

        if (requestDto.getSortBy() != null && !requestDto.getSortBy().isEmpty()) {
            sortBy = requestDto
                    .getSortBy()
                    .stream()
                    .filter(it -> ClassUtils.hasProperty(Article.class, it))
                    .map(ContentService::camelToSnake)
                    .toArray(String[]::new);
        }

        if (sortBy == null || sortBy.length == 0) {
            sortBy = new String[]{"id"};
        }

        if (StringUtils.hasText(requestDto.getQuery())) {

            if (requestDto.getQuery().startsWith("@")) {
                userId = resolveUserId(requestDto.getQuery());
                requestDto.setQuery(null);
            }

            if (requestDto.getQuery() != null && requestDto.getQuery().startsWith("#")) {
                tags = requestDto.getQuery().toLowerCase();
                requestDto.setQuery(null);
            }

            query = requestDto.getQuery();
        }

        if (!Objects.equals(userInfo.getCurrentUser().getId(), userId)) {
            statuses.add(Article.Status.PUBLISHED);
        } else if (Objects.equals(userInfo.getCurrentUser().getId(), userId)) {
            statuses.add(Article.Status.DRAFT);
            statuses.add(Article.Status.PUBLISHED);
        }

        var pageRequest = PageRequest.of(
                requestDto.getPage(),
                requestDto.getMax(),
                Sort.by(
                        requestDto.getDirection(),
                        sortBy
                )
        );

        Page<Article> page;

        // TODO refactor
        if (StringUtils.hasText(tags)) {
            page = articleRepo.findAllByTags(statuses.stream().map(Article.Status::name).toList(), tags, pageRequest);
        } else if (StringUtils.hasText(query)) {
            page = articleRepo.findAllByContent(statuses.stream().map(Article.Status::name).toList(), query, pageRequest);
        } else if (userId != null) {
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
                            it.getTags(),
                            it.getStatus().name().toLowerCase())
                    )
                    .toList()
            );
        }

        return new ListContentResponseDto(resList, page.getTotalPages());
    }

    @Transactional
    public TagsResponseDto tags(@NonNull TagsRequestDto requestDto) {
        var pageRequest = PageRequest.of(requestDto.getPage(), requestDto.getMax());

        Page<String> page = null;

        if (StringUtils.hasText(requestDto.getQuery())) {
            page = articleRepo.getTags(List.of(Article.Status.PUBLISHED.name()), requestDto.getQuery(), pageRequest);
        } else {
            page = articleRepo.getTags(List.of(Article.Status.PUBLISHED.name()), pageRequest);
        }

        List<String> list = page.isEmpty() ? Collections.emptyList() : page.getContent();

        return new TagsResponseDto(list, page.getTotalPages());
    }

    private String resolveUserName(Long id) {
        return userRepo.getNickNameById(id).orElseGet(() -> () -> "unknown").getNickName();
    }

    private Long resolveUserId(String nickName) {
        return userRepo.getIdByNickName(nickName).orElseGet(() -> () -> -1L).getId();
    }

    private static String camelToSnake(String str) {
        StringBuilder result = new StringBuilder();

        char c = str.charAt(0);
        result.append(Character.toLowerCase(c));

        for (int i = 1; i < str.length(); i++) {

            char ch = str.charAt(i);

            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }
}
