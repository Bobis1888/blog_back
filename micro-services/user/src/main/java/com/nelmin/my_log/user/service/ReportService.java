package com.nelmin.my_log.user.service;

import com.nelmin.my_log.user.dto.ReportRequestDto;
import com.nelmin.my_log.user.model.Report;
import com.nelmin.my_log.user_info.core.UserInfo;
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

            if (reportRepository.existsByArticleIdAndUserId(requestDto.articleId(), userInfo.getId())) {
                return;
            }

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

    // TODO
//    @Transactional
//    @Scheduled(fixedDelay = 2L, timeUnit = TimeUnit.HOURS)
//    public void processReport() {
//        log.info("Start to process report");
//        var articleToSave = new ArrayList<Article>();
//
//        reportRepo.countReport().forEach(it -> {
//
//            if (it.getCount() >= maxReportCount) {
//                articleRepo.findById(it.getId()).ifPresent(article -> {
//                    article.setStatus(Article.Status.BLOCKED);
//                    article.setPublishedDate(null);
//                    articleToSave.add(article);
//                });
//            }
//        });
//
//        if (!articleToSave.isEmpty()) {
//            log.info("Found {} articles to block", articleToSave.size());
//            articleRepo.saveAll(articleToSave);
//
//            var builder = new StringBuilder();
//            articleToSave.forEach(it -> builder.append(it.getId()).append(", "));
////            reportRepo.deleteAllByArticleIdIn(articleToSave.stream().map(Article::getId).toList());
//        }
//
//        log.info("End to process report");
//    }
}
