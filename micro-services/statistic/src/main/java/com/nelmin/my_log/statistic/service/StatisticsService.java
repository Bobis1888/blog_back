package com.nelmin.my_log.statistic.service;

import com.nelmin.my_log.statistic.dto.StatisticsResponseDto;
import com.nelmin.my_log.statistic.model.UserStatistic;
import com.nelmin.my_log.user_info.core.UserInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserInfo userInfo;
    private final UserStatistic.Repo userStatisticRepo;

    @Transactional
    public StatisticsResponseDto getStatistics() {
        var res = new StatisticsResponseDto();
        userStatisticRepo.findByUserId(userInfo.getId())
                .ifPresent(res::setStatistics);
        return res;
    }

    @Transactional
    public List<StatisticsResponseDto> getStatistics(@NonNull List<Long> ids) {
        var res = new ArrayList<StatisticsResponseDto>();

        userStatisticRepo.findAllByUserIdIn(ids)
                .stream()
                .map(this::getStat)
                .forEach(res::add);

        return res;
    }

    private StatisticsResponseDto getStat(UserStatistic statistic) {
        var res = new StatisticsResponseDto();
        res.setStatistics(statistic);
        return res;
    }
}
