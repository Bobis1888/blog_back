package com.nelmin.my_log.subscription.service;

import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.subscription.dto.*;
import com.nelmin.my_log.user_info.core.UserInfo;
import com.nelmin.my_log.subscription.model.Subscription;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionsService {

    private final UserInfo userInfo;
    private final Subscription.Repo subscriptionRepo;

    @Transactional
    public SuccessDto subscribe(Long authorId) {
        var res = false;

        try {

            if (authorId.equals(userInfo.getId())) {
                log.warn("Can not subscribe self {}", userInfo.getId());
                return new SuccessDto(false);
            }

            var subscription = subscriptionRepo.findByUserIdAndAuthorId(userInfo.getId(), authorId);


            if (subscription.isEmpty()) {
                var subs = Subscription.builder()
                        .userId(userInfo.getId())
                        .authorId(authorId)
                        .build();
                subscriptionRepo.save(subs);
                res = true;
            } else {
                log.warn("Already subscribed {} {}", userInfo.getId(), authorId);
            }

        } catch (Exception ex) {
            log.error("Error subscribe", ex);
        }

        return new SuccessDto(res);
    }

    @Transactional
    public SuccessDto unsubscribe(Long authorId) {
        var res = false;

        try {
            var subscription = subscriptionRepo.existsByUserIdAndAuthorId(userInfo.getId(), authorId);

            if (subscription) {
                subscriptionRepo.deleteByUserIdAndAuthorId(userInfo.getId(), authorId);
                res = true;
            } else {
                log.info("User {} not subscribed {}", userInfo.getId(), authorId);
            }
        } catch (Exception ex) {
            log.error("Error unsubscribe", ex);
        }

        return new SuccessDto(res);
    }

    @Transactional
    public ListResponseDto list(@NonNull ListRequestDto dto) {
        var res = new ListResponseDto();

        try {
            Page<Subscription> page;

            switch (dto.getType()) {
                case SUBSCRIPTIONS:
                    page = subscriptionRepo.findAllByUserId(userInfo.getId(), createPageRequest(dto));
                    break;
                case SUBSCRIBERS:
                    page = subscriptionRepo.findAllByAuthorId(userInfo.getId(), createPageRequest(dto));
                    break;
                default:
                    return res;
            }

            pageToResponse(page, res);
        } catch (Exception ex) {
            log.error("Error get subscriptions", ex);
        }

        return res;
    }

    private void pageToResponse(@NonNull Page<Subscription> page, @NonNull ListResponseDto res) {

        if (!page.isEmpty()) {
            res.setList(
                    page.getContent()
                            .stream()
                            .map(it -> new SubscriptionDto(
                                    it.getAuthorId(),
                                    it.getCreatedDate()
                            )).toList()
            );

            res.setTotalPages(page.getTotalPages());
            res.setTotalRows(page.getTotalElements());
        }
    }

    public List<ActionsDto> actions(@NonNull List<Long> userIds) {
        var list = new ArrayList<ActionsDto>();

        List<Long> subscriptions = subscriptionRepo
                .findAllByUserIdAndAuthorIdIn(userInfo.getId(), userIds)
                .stream()
                .map(Subscription.AuthorId::getAuthorId)
                .toList();

        // TODO refactor
        userIds.forEach((Long it) -> {
            var contains = subscriptions.contains(it);
            list.add(new ActionsDto(it, !contains, contains));
        });

        return list;
    }

    private PageRequest createPageRequest(@NonNull ListRequestDto requestDto) {
        return PageRequest.of(
                requestDto.getPage(),
                requestDto.getMax(),
                Sort.by(
                        requestDto.getDirection(),
                        "id")
        );
    }
}
