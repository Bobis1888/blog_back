package com.nelmin.blog.content.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.service.FillStatisticInfo;
import com.nelmin.blog.common.service.UserService;
import com.nelmin.blog.content.dto.StatisticsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        try {
            res.setUserid(userInfo.getId());
            fillInfoList.forEach(it -> it.fillStatisticInfo(res));
        } catch (Exception ex) {
            log.error("Error get statistics", ex);
        }

        return res;
    }

    @Transactional
    public StatisticsResponseDto getStatistics(String nickname) {
        var res = new StatisticsResponseDto();

        try {
            res.setUserid(userService.resolveId(nickname));
            fillInfoList.forEach(it -> it.fillStatisticInfo(res));
        } catch (Exception ex) {
            log.error("Error get statistics", ex);
        }

        return res;
    }
}
