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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

        if (article.getStatus() == Article.Status.PUBLISHED) {
            response.setSuccess(false);
            response.reject("notEditable", "status");
            return response;
        }

        if (article.getStatus() == Article.Status.PENDING) {
            response.setSuccess(false);
            response.reject("notEditable", "status");
            return response;
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
                article.get().setStatus(Article.Status.DELETED);
                articleRepo.save(article.get());
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
                (List.of(Article.Status.DRAFT, Article.Status.PENDING).contains(article.get().getStatus()) &&
                        Objects.equals(article.get().getUserId(), userInfo.getCurrentUser().getId())))) {
            res.setId(article.get().getId());
            res.setContent(article.get().getContent());
            res.setTitle(article.get().getTitle());
            res.setStatus(article.get().getStatus().name().toLowerCase());
            res.setPublishedDate(article.get().getPublishedDate());
            res.setTags(article.get().getTags());
            res.setAuthorName(resolveUserName(article.get().getUserId()));
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

                if (status == Article.Status.PUBLISHED &&
                        List.of(Article.Status.PUBLISHED, Article.Status.PENDING).contains(article.get().getStatus())) {

                    if (article.get().getStatus() == Article.Status.PENDING) {
                        response.reject("pendingPublish", "status");
                    } else {
                        response.reject("alreadyPublished", "status");
                    }
                }

                if (!StringUtils.hasText(article.get().getContent())) {
                    response.reject("nullable", "content");
                }

                if (!StringUtils.hasText(article.get().getTitle())) {
                    response.reject("nullable", "title");
                }

                if (response.hasErrors()) {
                    response.setSuccess(false);
                    return response;
                }

                if (status == Article.Status.PUBLISHED) {
                    status = Article.Status.PENDING;
                }

                article.get().setStatus(status);

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

    //TODO refactor
    @Transactional
    public ListContentResponseDto list(ListContentRequestDto requestDto) {
        List<ArticleDto> resList = new ArrayList<>();
        String[] sortBy = null;

        List<String> statuses = new ArrayList<>();
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

        if (requestDto.getSearch() != null) {
            var search = requestDto.getSearch();

            if (StringUtils.hasText(search.getQuery())) {
                query = search.getQuery();
            }

            if (StringUtils.hasText(search.getAuthor())) {
                userId = resolveUserId(search.getAuthor());
            }

            if (search.getTags() != null && !search.getTags().isEmpty()) {
                tags = String.join("|", search.getTags());
            }
        }

        if (!Objects.equals(userInfo.getCurrentUser().getId(), userId)) {
            statuses.add(Article.Status.PUBLISHED.name());
        } else if (Objects.equals(userInfo.getCurrentUser().getId(), userId)) {
            statuses.add(Article.Status.DRAFT.name());
            statuses.add(Article.Status.PUBLISHED.name());
            statuses.add(Article.Status.PENDING.name());
            statuses.add(Article.Status.DELETED.name());
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

        // TODO refactor Specification
        if (StringUtils.hasText(tags)) {
            page = articleRepo.findAllByTags(statuses, tags, pageRequest);
        } else if (userId != null) {
            page = articleRepo.findAllByUserId(userId, statuses, pageRequest);
        } else if (StringUtils.hasText(query)) {
            page = articleRepo.findAllByContent(statuses, query, pageRequest);
        } else {
            page = articleRepo.findAllByStatus(statuses, pageRequest);
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
        Set<String> result = new HashSet<>();
        Page<String> page = null;

        if (StringUtils.hasText(requestDto.getQuery())) {
            page = articleRepo.getTags(List.of(Article.Status.PUBLISHED.name()), requestDto.getQuery(), pageRequest);
        } else {
            page = articleRepo.getTags(List.of(Article.Status.PUBLISHED.name()), pageRequest);
        }

        List<String> list = page.isEmpty() ? Collections.emptyList() : page.getContent();

        list.forEach(it -> {
            if (it.contains(",")) {
                result.addAll(Arrays.asList(it.split(",")));
            } else {
                result.add(it);
            }
        });

        return new TagsResponseDto(result, page.getTotalPages());
    }

    private String resolveUserName(Long id) {
        return userRepo.getNickNameById(id).orElseGet(() -> () -> "unknown").getNickName();
    }

    private Long resolveUserId(String nickName) {

        if (!nickName.startsWith("@")) {
            nickName = "@" + nickName;
        }

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

    @Transactional
    @Scheduled(fixedDelay = 12L, timeUnit = TimeUnit.HOURS)
    public void clearDeletedArticle() {
        log.info("Start to clear deleted article");

        try {
            var pageRequest = PageRequest.of(0, 1000);
            var ids = articleRepo.getIdsByStatusIn(List.of(Article.Status.DELETED), pageRequest)
                    .map(Article.ArticleId::getId).toList();
            log.info("Ids to clear deleted article: {}", ids);
            articleRepo.deleteAllByIdInBatch(ids);
        } catch (Exception ex) {
            log.error("Error clear deleted article", ex);
        }

        log.info("End to clear deleted article");
    }

    // TODO
    @Transactional
    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.HOURS)
    public void processArticle() {
        log.info("Start to process pending article");

        try {
            var pageRequest = PageRequest.of(0, 100);
            var page = articleRepo.findAllByStatus(List.of(Article.Status.PENDING.name()), pageRequest);

            if (!page.isEmpty()) {
                page.getContent().forEach(it -> {

                    try {
                        // TODO send to checker service and send email after checking
                        it.setStatus(Article.Status.PUBLISHED);
                        articleRepo.save(it);
                    } catch (Exception ex) {
                        log.error("Error process article", ex);
                    }
                });
            }
        } catch (Exception ex) {
            log.error("Error process articles", ex);
        }

        log.info("End to process pending article");
    }
}
