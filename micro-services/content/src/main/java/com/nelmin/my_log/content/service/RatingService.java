package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.service.FillStatisticInfo;
import com.nelmin.my_log.content.dto.StatisticsResponseDto;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.model.Reaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.nelmin.my_log.content.model.Article.Status.PUBLISHED;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService implements FillStatisticInfo<StatisticsResponseDto> {

    private final Article.Repo articleRepo;
    private final Reaction.Repo reactionRepo;

    //    @Cacheable(value = "ratings", key = "#userId")
    public Long calculateRating(Long userId) {
        Long res = 0L;

        if (userId == null) {
            return res;
        }

        try {
            res += reactionRepo.countByUserId(userId)
                    .stream()
                    .map(Reaction.CountReaction::getCount)
                    .reduce(0L, Long::sum);
            res += articleRepo.countByStatusAndUserId(PUBLISHED, userId);
        } catch (Exception exception) {
            log.error("Error calculate rating", exception);
        }

        return res;
    }

    @Override
    public void fillStatisticInfo(StatisticsResponseDto response) {
        response.setRating(calculateRating(response.getUserid()));
    }
}
