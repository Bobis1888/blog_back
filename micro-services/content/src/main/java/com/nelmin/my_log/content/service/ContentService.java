package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.content.Utils;
import com.nelmin.my_log.content.dto.common.*;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.model.specification.ContentSpecificationFactory;
import com.nelmin.my_log.content.model.PrivateLink;
import com.nelmin.my_log.user_info.core.UserInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nelmin.my_log.content.model.Article.Status.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final UserInfo userInfo;
    private final Article.Repo articleRepo;
    private final CommonHttpClient commonHttpClient;
    private final PrivateLink.Repo privateLinkRepo;
    private final PrivateLinkService privateLinkService;
    private final ContentProcessorService contentProcessorService;
    private final ContentSpecificationFactory contentSpecificationFactory;

    private static final String CLEAR_TAG_REGEXP = "[^#a-zA-Zа-яА-Я0-9_]";

    @Transactional
    public CreateContentResponseDto save(@NonNull CreateContentRequestDto dto) {
        log.info("create content: {}", dto);
        var response = new CreateContentResponseDto();
        Article article = null;

        if (dto.id() != null) {
            article = articleRepo.findByIdAndUserId(dto.id(), userInfo.getId()).orElseGet(Article::new);
        }

        if (article == null) {
            article = new Article();
        }

        if (article.getStatus() == PUBLISHED && !userInfo.isPremiumUser() || article.getStatus() == BLOCKED) {
            response.setSuccess(false);
            response.reject("notEditable", "status");
            return response;
        }

        if (article.getStatus() == PENDING) {
            response.setSuccess(false);
            response.reject("notEditable", "status");
            return response;
        }

        article.setUserId(userInfo.getId());
        article.setTitle(dto.title());
        contentProcessorService.process(article, dto.content());

        if (dto.tags() != null) {
            article.setTags(dto.tags().stream().map(it -> {

                        if (it == null || it.length() < 2) {
                            return null;
                        }

                        it = it.replaceAll(CLEAR_TAG_REGEXP, "");

                        if (!it.startsWith("#")) {
                            it = "#" + it;
                        }

                        return it;
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }

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

            if (List.of(DELETED, BLOCKED).contains(article.get().getStatus())) {
                response.reject("notEditable", "status");
                return response;
            }

            try {
                article.get().setStatus(DELETED);
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
    public ArticleDto get(@NonNull String privateLink) {

        try {
            var id = privateLinkRepo.getArticleIdByLink(privateLink);

            if (id.isPresent()) {
                return get(id.get().getArticleId());
            }
        } catch (Exception ex) {
            log.error("Error get article", ex);
        }

        var res = new ArticleDto();
        res.reject("notFound", "article");
        return res;
    }

    @Transactional
    public ArticleDto get(@NonNull Long id) {
        var res = new ArticleDto();
        var article = articleRepo.findById(id);

        if (article.isPresent() && (List.of(PUBLISHED, PRIVATE_PUBLISHED).contains(article.get().getStatus()) ||
                (article.get().getStatus() != DELETED && Objects.equals(article.get().getUserId(), userInfo.getId())))) {
            res.setId(article.get().getId());
            res.setContent(article.get().getContent());
            res.setTitle(article.get().getTitle());
            res.setStatus(article.get().getStatus().name().toLowerCase());
            res.setPublishedDate(article.get().getPublishedDate());
            res.setTags(article.get().getTags());
            res.setPreView(article.get().getPreView());
            res.setCountViews(article.get().getCountViews());
            res.setAuthorId(article.get().getUserId());

            // TODO remove
            resolveNicknames(article.get().getUserId().toString())
                    .ifPresent(map -> res.setAuthorName(String.valueOf(map.get(article.get().getUserId().toString()))));

            if (List.of(PUBLISHED, PRIVATE_PUBLISHED).contains(article.get().getStatus()) && !article.get().getUserId().equals(userInfo.getId())) {

                try {
                    articleRepo.increaseCountViews(article.get().getId());
                } catch (Exception ex) {
                    log.error("Error increase count views", ex);
                }
            }
        } else {
            res.reject("notFound", "article");
        }

        return res;
    }

    @Transactional
    public PublishContentResponseDto changeStatus(@NonNull Long id, @NonNull Article.Status status) {

        if (status == PRIVATE_PUBLISHED && !userInfo.isPremiumUser()) {
            status = PUBLISHED;
        }

        var response = new PublishContentResponseDto();
        var article = articleRepo.findByIdAndUserId(id, userInfo.getId());

        if (article.isPresent()) {

            try {

                if (List.of(PUBLISHED, PRIVATE_PUBLISHED).contains(status)
                        && List.of(PUBLISHED, PENDING, PRIVATE_PUBLISHED).contains(article.get().getStatus())) {

                    if (article.get().getStatus() == PENDING) {
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

                if (status == PRIVATE_PUBLISHED && userInfo.isPremiumUser()) {
                    var link = privateLinkService.generate(article.get().getId());
                    response.setLink(link);
                }

                if (PUBLISHED == status && !userInfo.isPremiumUser()) {
                    status = PENDING;
                }

                if (status == DRAFT) {
                    article.get().setPublishedDate(null);
                }

                if (List.of(PUBLISHED, PRIVATE_PUBLISHED).contains(status)) {
                    article.get().setPublishedDate(LocalDateTime.now());
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
    public ListContentResponseDto list(ListContentRequestDto requestDto) {
        var search = Optional.ofNullable(requestDto.getSearch())
                .orElse(new ListContentRequestDto.Search());

        var specRequest = new ContentSpecificationFactory.SpecificationRequestDto(
                requestDto.getType(),
                search.getQuery(),
                search.getExclude(),
                search.getStartDate(),
                search.getEndDate(),
                requestDto.getDirection()
        );

        var spec = contentSpecificationFactory.getSPecification(specRequest);
        PageRequest pageRequest = createPageRequest(requestDto);
        Page<Article> page = articleRepo.findAll(spec, pageRequest);

        return pageToDto(page);
    }

    private ListContentResponseDto pageToDto(@NonNull Page<Article> page) {
        List<ArticleDto> resList = new ArrayList<>();

        if (!page.isEmpty()) {
            String ids = page.getContent()
                    .stream()
                    .map(it -> it.getUserId().toString())
                    .collect(Collectors.joining(","));

            var response = resolveNicknames(ids);

            resList.addAll(page.getContent().stream().map(it -> {
                var res = new ArticleDto(it);
                res.setContent(null);
                res.setAuthorId(it.getUserId());
                response.ifPresent(map -> res.setAuthorName(String.valueOf(map.get(it.getUserId().toString()))));
                res.setCountViews(it.getCountViews());
                res.setCountComments(it.getStatistic().getComments());
                res.setCountReactions(it.getStatistic().getReactions());
                return res;
            }).toList());
        }

        return new ListContentResponseDto(resList, page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public SuccessDto changePreview(@NonNull Long id, @NonNull ChangePreviewRequestDto dto) {
        var response = new SuccessDto(false);

        try {
            var article = articleRepo.findByIdAndUserId(id, userInfo.getId());

            if (article.isPresent()) {

                if (List.of(PUBLISHED, PRIVATE_PUBLISHED).contains(article.get().getStatus())
                        && !userInfo.isPremiumUser() || article.get().getStatus() == BLOCKED) {
                    response.reject("notEditable", "status");
                } else {
                    article.get().setPreView(dto.content());
                    articleRepo.save(article.get());
                    response.setSuccess(true);
                }
            } else {
                response.reject("notFound", "article");
            }
        } catch (Exception ex) {
            log.error("Error change preview", ex);
            response.reject("serverError");
        }

        response.setSuccess(response.hasErrors());
        return response;
    }

    private PageRequest createPageRequest(@NonNull ListContentRequestDto requestDto) {
        String[] sortBy;
        Sort sort = null;

        if (requestDto.getSortBy() != null && !requestDto.getSortBy().isEmpty()) {
            sortBy = Utils.getSortProperties(requestDto.getSortBy(), Article.class);

            if (sortBy.length != 0) {
                sort = Sort.by(
                        Optional.ofNullable(requestDto.getDirection()).orElse(Sort.Direction.DESC),
                        sortBy);
            }
        }

        if (sort == null) {
            return PageRequest.of(requestDto.getPage(), requestDto.getMax());
        } else {
            return PageRequest.of(requestDto.getPage(), requestDto.getMax(), sort);
        }
    }

    private Optional<Map> resolveNicknames(String ids) {
        return commonHttpClient.exchange("user/info/resolve_nicknames?ids=" + ids, HttpMethod.GET, Map.class);
    }
}
