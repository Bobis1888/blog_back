package com.nelmin.blog.content.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.dto.SuccessDto;
import com.nelmin.blog.common.service.FillStatisticInfo;
import com.nelmin.blog.common.service.UserService;
import com.nelmin.blog.content.Utils;
import com.nelmin.blog.content.dto.*;
import com.nelmin.blog.content.model.Article;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService implements FillStatisticInfo<StatisticsResponseDto> {

    private final Article.Repo articleRepo;
    private final UserInfo userInfo;
    private final UserService userService;
    private final SubscriptionsService subscriptionsService;

    @Transactional
    public CreateContentResponseDto update(@NonNull Long id, @NonNull CreateContentRequestDto dto) {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Transactional
    public CreateContentResponseDto create(@NonNull CreateContentRequestDto dto) {
        log.info("create content: {}", dto);
        var response = new CreateContentResponseDto();
        Article article = null;

        if (dto.id() != null) {
            article = articleRepo.findByIdAndUserId(dto.id(), userInfo.getId()).orElseGet(Article::new);
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

        article.setUserId(userInfo.getId());

        // TODO validate html/sql injection
        article.setContent(dto.content());

        if (dto.preView() == null || StringUtils.hasText(dto.preView()) && dto.preView().equals("auto")) {
            var generatedPreview = dto.content().substring(0, Math.min(dto.content().length(), 255));
            article.setPreView(generatedPreview);
        } else {
            article.setPreView(dto.preView());
        }

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

        var article = articleRepo.findByIdAndUserId(id, userInfo.getId());

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

        if (article.isPresent() && (article.get().getStatus() == Article.Status.PUBLISHED || (List.of(Article.Status.DRAFT, Article.Status.PENDING).contains(article.get().getStatus()) && Objects.equals(article.get().getUserId(), userInfo.getId())))) {
            res.setId(article.get().getId());
            res.setContent(article.get().getContent());
            res.setTitle(article.get().getTitle());
            res.setStatus(article.get().getStatus().name().toLowerCase());
            res.setPublishedDate(article.get().getPublishedDate());
            res.setTags(article.get().getTags());

            if (article.get().getStatus() != Article.Status.PUBLISHED) {
                res.setPreView(article.get().getPreView());
            }

            res.setAuthorName(userService.resolveNickname(article.get().getUserId()));
        } else {
            res.reject("notFound", "article");
        }

        return res;
    }

    @Transactional
    public PublishContentResponseDto changeStatus(@NonNull Long id, @NonNull Article.Status status) {
        var response = new PublishContentResponseDto();
        var article = articleRepo.findByIdAndUserId(id, userInfo.getId());

        if (article.isPresent()) {

            try {

                if (status == Article.Status.PUBLISHED && List.of(Article.Status.PUBLISHED, Article.Status.PENDING).contains(article.get().getStatus())) {

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

                if (status == Article.Status.DRAFT) {
                    article.get().setPublishedDate(null);
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

    @Transactional
    public ListContentResponseDto all(ListContentRequestDto requestDto) {
        return search(
                List.of(userInfo.getId()),
                List.of(Article.Status.PUBLISHED.name(), Article.Status.DRAFT.name(), Article.Status.PENDING.name()),
                null,
                null,
                createPageRequest(requestDto));
    }

    @Transactional
    public ListContentResponseDto listFromAuthors(ListContentRequestDto requestDto) {
        var userIds = subscriptionsService.subscriptions();

        if (userIds == null || userIds.isEmpty()) {
            return new ListContentResponseDto(new ArrayList<>(), 0L, 0);
        }

        return search(
                userIds,
                List.of(Article.Status.PUBLISHED.name()),
                null,
                null,
                createPageRequest(requestDto));
    }

    //TODO refactor
    @Transactional
    public ListContentResponseDto list(ListContentRequestDto requestDto) {
        String[] sortBy = null;
        List<Long> userIds = new ArrayList<>();
        String query = null;
        String tags = null;

        if (requestDto.getSortBy() != null && !requestDto.getSortBy().isEmpty()) {
            sortBy = Utils.getSortProperties(requestDto.getSortBy(), Article.class);
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
                userIds = userService.resolveUserIds(search.getAuthor());
            }

            if (search.getTags() != null && !search.getTags().isEmpty()) {
                tags = String.join("|", search.getTags());
            }
        }

        var pageRequest = PageRequest.of(requestDto.getPage(), requestDto.getMax(), Sort.by(requestDto.getDirection(), sortBy));

        return search(userIds, List.of(Article.Status.PUBLISHED.name()), tags, query, pageRequest);
    }

    private ListContentResponseDto search(List<Long> userIds, List<String> statuses, String tags, String query, PageRequest pageRequest) {
        List<ArticleDto> resList = new ArrayList<>();

        Page<Article> page = null;

        // TODO refactor Specification
        if (StringUtils.hasText(tags)) {
            page = articleRepo.findAllByTags(statuses, tags, pageRequest);
        } else if (!userIds.isEmpty()) {
            page = articleRepo.findAllByUserIds(userIds, statuses, pageRequest);
        } else if (StringUtils.hasText(query)) {
            page = articleRepo.findAllByContent(statuses, query, pageRequest);
        } else {
            page = articleRepo.findAllByStatus(statuses, pageRequest);
        }

        if (!page.isEmpty()) {
            resList.addAll(page.getContent().stream().map(it -> {
                var res = new ArticleDto(it);
                res.setContent(null);
                res.setAuthorName(userService.resolveNickname(it.getUserId()));
                return res;
            }).toList());
        }

        return new ListContentResponseDto(resList, page.getTotalElements(), page.getTotalPages());
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

            if (it == null) {
                return;
            }

            if (it.contains(",")) {
                result.addAll(Set.of(it.split(",")));
            } else {
                result.add(it);
            }
        });

        return new TagsResponseDto(result, page.getTotalPages());
    }

    @Transactional
    public SuccessDto changePreview(@NonNull Long id, @NonNull ChangePreviewRequestDto dto) {
        var response = new SuccessDto(false);

        try {
            var article = articleRepo.findByIdAndUserId(id, userInfo.getId());

            if (article.isPresent()) {
                article.get().setPreView(dto.content());
                articleRepo.save(article.get());
                response.setSuccess(true);
            } else {
                response.reject("notFound", "article");
            }
        } catch (Exception ex) {
            log.error("Error change preview", ex);
        }

        return response;
    }

    @Override
    public void fillStatisticInfo(@NonNull StatisticsResponseDto response) {
        response.setArticles(articleRepo.countByStatusAndUserId(Article.Status.PUBLISHED, response.getUserid()));
    }

    private PageRequest createPageRequest(ListContentRequestDto requestDto) {
        return PageRequest.of(requestDto.getPage(), requestDto.getMax(), Sort.by(requestDto.getDirection(), "id"));
    }
}
