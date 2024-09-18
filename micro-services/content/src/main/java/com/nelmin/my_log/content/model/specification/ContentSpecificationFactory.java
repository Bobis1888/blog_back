package com.nelmin.my_log.content.model.specification;

import java.time.LocalDate;
import java.util.*;

import com.nelmin.my_log.content.dto.common.ListSubscriptionRequestDto;
import com.nelmin.my_log.content.dto.common.ListSubscriptionResponseDto;
import com.nelmin.my_log.content.dto.common.UserInfoDto;
import com.nelmin.my_log.content.model.*;
import com.nelmin.my_log.content.service.CommonHttpClient;
import com.nelmin.my_log.user_info.core.UserInfo;
import jakarta.persistence.criteria.*;
import lombok.NonNull;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import static com.nelmin.my_log.content.model.Article.Status.*;

//TODO some refactor
@Component
@RequiredArgsConstructor
public class ContentSpecificationFactory {

    private final UserInfo userInfo;
    private final CommonHttpClient httpClient;

    public enum RequestType {
        TOP,
        MY,
        BOOKMARK,
        SUBSCRIPTION,
        SEARCH;

        RequestType() {
        }
    }

    public record SpecificationRequestDto(
            @NonNull RequestType type,
            String query,
            List<Long> exclude,
            LocalDate startDate,
            LocalDate endDate,
            Sort.Direction direction) {
    }

    public Specification<Article> getSPecification(@NonNull SpecificationRequestDto requestDto) {
        return switch (requestDto.type()) {
            case TOP -> topCriteria(requestDto);
            case MY -> myCriteria();
            case BOOKMARK -> bookmarkCriteria();
            case SUBSCRIPTION -> subscriptionsCriteria(requestDto);
            case SEARCH -> searchCriteria(requestDto);
        };
    }

    private Specification<Article> topCriteria(@NonNull SpecificationRequestDto requestDto) {
        return (root, query, cb) -> {
            Join<Article, ArticleStatistic> join = root.join("statistic", JoinType.INNER);

            Predicate predicate = statusPredicate(requestDto, root, PUBLISHED);

            predicate = startDateEndDatePredicate(predicate, requestDto, root, cb);
            predicate = excludeIdPredicate(predicate, requestDto, root, cb);

            if (StringUtils.hasText(requestDto.query())) {
                predicate = cb.and(
                        predicate,
                        cb.like(root.get(Article_.TITLE), "%" + requestDto.query() + "%")
                );
            }

            List<Order> orders;

            if (requestDto.direction() == Sort.Direction.DESC) {
                orders = List.of(
                        cb.desc(join.get(ArticleStatistic_.REACTIONS)),
                        cb.desc(join.get(ArticleStatistic_.VIEWS)),
                        cb.desc(root.get(Article_.PUBLISHED_DATE))
                );
            } else {
                orders = List.of(
                        cb.asc(join.get(ArticleStatistic_.REACTIONS)),
                        cb.asc(join.get(ArticleStatistic_.VIEWS)),
                        cb.asc(root.get(Article_.PUBLISHED_DATE))
                );
            }

            query.orderBy(orders);

            return predicate;
        };
    }

    private Specification<Article> myCriteria() {
        return (root, query, cb) -> {

            query.orderBy(
                    cb.desc(root.get(Article_.PUBLISHED_DATE))
            );

            return cb.and(
                    cb.in(root.get(Article_.STATUS)).value(Arrays.stream(values())
                            .filter(it -> it != DELETED).map(Enum::name).toList()),
                    cb.equal(root.get(Article_.USER_ID), userInfo.getId())
            );
        };

    }

    private Specification<Article> bookmarkCriteria() {
        return (root, query, cb) -> {
            Join<Article, Bookmark> join = root.join("bookmark", JoinType.INNER);

            query.orderBy(
                    cb.desc(join.get(Bookmark_.CREATED_DATE))
            );

            return cb.and(
                    cb.equal(join.get(Bookmark_.USER_ID), userInfo.getId()),
                    root.get(Article_.STATUS).in(PUBLISHED, PRIVATE_PUBLISHED)
            );
        };
    }

    private Specification<Article> searchCriteria(@NonNull SpecificationRequestDto requestDto) {
        return (root, query, cb) -> {

            Predicate predicate = statusPredicate(requestDto, root, PUBLISHED);

            predicate = startDateEndDatePredicate(predicate, requestDto, root, cb);
            predicate = excludeIdPredicate(predicate, requestDto, root, cb);

            if (StringUtils.hasText(requestDto.query())) {

                boolean specificSearch = requestDto.query().matches("^[#@].*");

                if (!specificSearch) {
                    predicate = cb.and(
                            predicate,
                            cb.or(
                                    cb.like(root.get(Article_.TITLE), "%" + requestDto.query() + "%"),
                                    cb.like(root.get(Article_.CONTENT), "%" + requestDto.query() + "%")
                            )
                    );
                }

                if (specificSearch) {

                    if (requestDto.query().startsWith("@")) {

                        var response = httpClient.exchange(
                                "user/info/" + requestDto.query().substring(1),
                                HttpMethod.GET,
                                List.class);

                        var ids = new ArrayList<Long>();

                        response.ifPresent(it ->
                                ids.addAll(it.stream()
                                        .map(userMap -> ((Map) userMap).get("id"))
                                        .toList()));

                        predicate = cb.and(
                                predicate,
                                root.get(Article_.USER_ID).in(ids)
                        );
                    } else {
                        var regex = String.join("|", requestDto.query().split(","));
                        Expression<Boolean> regExprLike = cb.function(
                                "textregexeq",
                                Boolean.class,
                                root.get(Article_.TAGS),
                                cb.literal(regex));

                        predicate = cb.and(
                                predicate,
                                cb.isTrue(regExprLike)
                        );
                    }
                }
            }

            if (requestDto.direction() == Sort.Direction.DESC) {
                query.orderBy(
                        cb.desc(root.get(Article_.PUBLISHED_DATE))
                );
            } else {
                query.orderBy(
                        cb.asc(root.get(Article_.PUBLISHED_DATE))
                );
            }

            return predicate;
        };
    }

    /**
     * example =>
     * select * from article art where art.user_id in
     * (select sb.author_id from subscription sb where sb.user_id = ?) and art.status = ?
     */
    private Specification<Article> subscriptionsCriteria(@NonNull SpecificationRequestDto requestDto) {
        return (root, query, cb) -> {

            var response = httpClient.exchange(
                    "subscription/list",
                    HttpMethod.POST,
                    new ListSubscriptionRequestDto(ListSubscriptionRequestDto.Type.SUBSCRIPTIONS, 100, 0),
                    ListSubscriptionResponseDto.class);

            var list = new ArrayList<Long>();

            response.ifPresent(it -> {

                if (Optional.ofNullable(it.totalRows()).orElse(0) < 1 || it.list() == null) {
                    return;
                }

                list.addAll(
                                it
                                        .list()
                                        .stream()
                                        .mapToLong(ListSubscriptionResponseDto.SubscriptionDto::userId)
                                        .boxed()
                                        .toList());
                    }
            );

            if (requestDto.direction() == Sort.Direction.DESC) {
                query.orderBy(
                        cb.desc(root.get(Article_.PUBLISHED_DATE))
                );
            } else {
                query.orderBy(
                        cb.asc(root.get(Article_.PUBLISHED_DATE))
                );
            }

            return cb.and(
                    root.get(Article_.STATUS).in(PUBLISHED),
                    root.get(Article_.USER_ID).in(list)
            );
        };
    }

    private Predicate statusPredicate(@NonNull SpecificationRequestDto requestDto,
                                      @NonNull Root<Article> root,
                                      Article.Status... statusList) {

        return root.get(Article_.STATUS).in((Object[]) statusList);
    }

    private Predicate startDateEndDatePredicate(@NonNull Predicate predicate,
                                                @NonNull SpecificationRequestDto requestDto,
                                                @NonNull Root<Article> root,
                                                @NonNull CriteriaBuilder cb) {

        if (requestDto.startDate() != null) {
            predicate = cb.and(
                    predicate,
                    cb.greaterThanOrEqualTo(root.get(Article_.PUBLISHED_DATE),
                            requestDto.startDate().atStartOfDay())
            );
        }

        if (requestDto.endDate() != null) {
            predicate = cb.and(
                    predicate,
                    cb.lessThanOrEqualTo(root.get(Article_.PUBLISHED_DATE),
                            requestDto.endDate().plusDays(1).atStartOfDay())
            );
        }

        return predicate;
    }

    private Predicate excludeIdPredicate(@NonNull Predicate predicate,
                                         @NonNull SpecificationRequestDto requestDto,
                                         @NonNull Root<Article> root,
                                         @NonNull CriteriaBuilder cb) {

        if (requestDto.exclude() != null && !requestDto.exclude().isEmpty()) {
            predicate = cb.and(
                    predicate,
                    cb.not(root.get(Article_.ID).in(requestDto.exclude()))
            );
        }

        return predicate;
    }
}
