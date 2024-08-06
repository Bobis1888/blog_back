package com.nelmin.my_log.auth.service;

import com.nelmin.my_log.auth.dto.ReportRequestDto;
import com.nelmin.my_log.common.exception.CommonException;
import com.nelmin.my_log.common.model.Report;
import com.nelmin.my_log.common.bean.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final Report.Repo reportRepository;
    private final UserInfo userInfo;

    public void report(ReportRequestDto requestDto) {

        try {

//            if (reportRepository.existsByArticleIdAndUserId(requestDto.articleId(), userInfo.getId())) {
//                return;
//            }

            var report = new Report();
            report.setUserId(userInfo.getId());
            report.setArticleId(requestDto.articleId());
            report.setType(requestDto.type());
            report.setDescription(requestDto.description());
            reportRepository.save(report);

        } catch (Exception ex) {
            log.error("Error report", ex);
        }
    }
}
