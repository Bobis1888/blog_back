package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.service.FillStatisticInfo;
import com.nelmin.my_log.common.service.UserService;
import com.nelmin.my_log.content.dto.StatisticsResponseDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserInfo userInfo;
    private final UserService userService;
    private final List<FillStatisticInfo<StatisticsResponseDto>> fillInfoList;

    @Transactional
    public StatisticsResponseDto getStatistics() {
        var res = new StatisticsResponseDto();
        res.setUserid(userInfo.getId());
        return getStat(res);
    }

    @Transactional
    public StatisticsResponseDto getStatistics(String nickname) {
        var res = new StatisticsResponseDto();
        res.setUserid(userService.resolveId(nickname));
        return getStat(res);
    }

    @Transactional
    public List<StatisticsResponseDto> getStatistics(@NonNull List<String> nicknames) {
        var res = new ArrayList<StatisticsResponseDto>();

        nicknames.stream()
                .filter(StringUtils::hasText)
                .filter(it -> !userInfo.isAuthorized() || !it.equals(userInfo.getNickname()))
                .forEach(it -> {
            var dto = new StatisticsResponseDto();
            dto.setNickname(it);
            dto.setUserid(userService.resolveId(it));
            res.add(getStat(dto));
        });

        return res;
    }

    private StatisticsResponseDto getStat(StatisticsResponseDto res) {

        try {
            fillInfoList.forEach(it -> it.fillStatisticInfo(res));
            res.setUserid(null);
        } catch (Exception ex) {
            log.error("Error get statistics", ex);
        }

        return res;
    }
}
