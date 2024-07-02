package com.nelmin.blog.content.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.dto.SuccessDto;
import com.nelmin.blog.common.exception.UserNotFoundException;
import com.nelmin.blog.common.service.FillStatisticInfo;
import com.nelmin.blog.common.service.UserService;
import com.nelmin.blog.content.dto.ListSubscriptionRequestDto;
import com.nelmin.blog.content.dto.ListSubscriptionResponseDto;
import com.nelmin.blog.content.dto.StatisticsResponseDto;
import com.nelmin.blog.content.dto.SubscriptionDto;
import com.nelmin.blog.content.model.Subscription;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionsService implements FillStatisticInfo<StatisticsResponseDto> {

    private final UserInfo userInfo;
    private final UserService userService;
    private final Subscription.Repo subscriptionRepo;

    @Transactional
    public SuccessDto subscribe(String authorNickname) {
        var res = new SuccessDto(false);

        try {
            var authorId = userService.resolveId(authorNickname);

            if (authorId.equals(userInfo.getId())) {
                log.warn("Can not subscribe self {}", userInfo.getId());
                res.reject("not_found", "author");
                return res;
            }

            var subscription = subscriptionRepo.findByUserIdAndAuthorId(userInfo.getId(), authorId);


            if (subscription.isEmpty()) {
                var subs = Subscription.builder()
                        .userId(userInfo.getId())
                        .authorId(authorId)
                        .build();
                subscriptionRepo.save(subs);
                res.setSuccess(true);
            } else {
                log.warn("Already subscribed {} {}", userInfo.getId(), authorId);
            }

        } catch (UserNotFoundException notFound) {
            log.info("User not found {}", authorNickname);
            log.debug("Error subscribe", notFound);
            res.reject("not_found", "author");
        } catch (Exception ex) {
            log.error("Error subscribe", ex);
            res.reject("internal_error", "subscribe");
        }

        return res;
    }

    @Transactional
    public SuccessDto unsubscribe(String authorNickname) {
        var res = new SuccessDto(false);

        try {
            var authorId = userService.resolveId(authorNickname);
            var subscription = subscriptionRepo.existsByUserIdAndAuthorId(userInfo.getId(), authorId);

            if (subscription) {
                subscriptionRepo.deleteByUserIdAndAuthorId(userInfo.getId(), authorId);
                res.setSuccess(true);
            } else {
                res.reject("not_found", "subscription");
            }
        } catch (UserNotFoundException notFound) {
            log.info("User not found {}", authorNickname);
            log.debug("Error subscribe", notFound);
            res.reject("not_found", "author");
        } catch (Exception ex) {
            log.error("Error unsubscribe", ex);
            res.reject("internal_error", "subscribe");
        }

        return res;
    }

    @Transactional
    public ListSubscriptionResponseDto subscribers(ListSubscriptionRequestDto dto) {
        var res = new ListSubscriptionResponseDto();

        try {
            var page = subscriptionRepo.findAllByAuthorId(userInfo.getId(), createPageRequest(dto));

            if (!page.isEmpty()) {
                res.setList(
                        page.getContent()
                                .stream()
                                .map(it -> new SubscriptionDto(
                                        userService.resolveNickname(it.getUserId()),
                                        it.getCreatedDate()
                                )).toList()
                );
                res.setTotalPages(page.getTotalPages());
                res.setTotalRows(page.getTotalElements());
            }

        } catch (Exception ex) {
            log.error("Error get subscribers", ex);
            res.reject("internal_error", "subscribers");
        }

        return res;
    }

    @Transactional
    public ListSubscriptionResponseDto subscriptions(ListSubscriptionRequestDto dto) {
        var res = new ListSubscriptionResponseDto();

        try {
            var page = subscriptionRepo.findAllByUserId(userInfo.getId(), createPageRequest(dto));

            if (!page.isEmpty()) {
                res.setList(
                        page.getContent()
                                .stream()
                                .map(it -> new SubscriptionDto(
                                        userService.resolveNickname(it.getAuthorId()),
                                        it.getCreatedDate()
                                )).toList()
                );

                res.setTotalPages(page.getTotalPages());
                res.setTotalRows(page.getTotalElements());
            }
        } catch (Exception ex) {
            log.error("Error get subscriptions", ex);
            res.reject("internal_error", "subscriptions");
        }

        return res;
    }

    @Transactional
    public List<Long> subscriptions() {
        return subscriptionRepo.findAllByUserId(userInfo.getId())
                .stream()
                .map(Subscription.AuthorProjection::getAuthorId)
                .toList();
    }

    @Override
    public void fillStatisticInfo(StatisticsResponseDto response) {
        response.setSubscribers(subscriptionRepo.countByAuthorId(response.getUserid()));
        response.setSubscriptions(subscriptionRepo.countByUserId(response.getUserid()));

        if (!Objects.equals(userInfo.getId(), response.getUserid())) {
            response.setIsSubscriber(isSubscribed(userInfo.getId(), response.getUserid()));
            response.setUserIsSubscribed(isSubscribed(response.getUserid()));
        }
    }

    public Boolean isSubscribed(String authorNickname) {

        try {
            var authorId = userService.resolveId(authorNickname);
            return subscriptionRepo.existsByUserIdAndAuthorId(userInfo.getId(), authorId);
        } catch (Exception ex) {
            log.error("Error get isSubscribed", ex);
        }

        return false;
    }

    public Boolean isSubscribed(Long authorId) {
        return isSubscribed(authorId, userInfo.getId());
    }

    public Boolean isSubscribed(Long authorId, Long userId) {

        try {
            return subscriptionRepo.existsByUserIdAndAuthorId(userId, authorId);
        } catch (Exception ex) {
            log.error("Error get isSubscribed", ex);
        }

        return false;
    }

    private PageRequest createPageRequest(@NonNull ListSubscriptionRequestDto requestDto) {
        return PageRequest.of(
                requestDto.getPage(),
                requestDto.getMax(),
                Sort.by(
                        requestDto.getDirection(),
                        "id")
        );
    }
}
