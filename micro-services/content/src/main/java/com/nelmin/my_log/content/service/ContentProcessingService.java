package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.model.Report;
import com.nelmin.my_log.common.model.User;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.model.PrivateLink;
import com.nelmin.my_log.content.model.Reaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentProcessingService {

    @Value("${content.max_report_count:10}")
    private Long maxReportCount;

    private final Article.Repo articleRepo;
    private final User.Repo userRepository;
    private final Reaction.Repo reactionRepo;
    private final PrivateLink.Repo privateLinkRepo;
    private final Report.Repo reportRepo;
    private final RatingService ratingService;

    @Transactional
    @Scheduled(fixedDelay = 12L, timeUnit = TimeUnit.HOURS)
    public void clearDeletedArticle() {
        log.info("Start to clear deleted article");

        try {
            var pageRequest = PageRequest.of(0, 1000);
            var ids = articleRepo.getIdsByStatusIn(List.of(Article.Status.DELETED), pageRequest)
                    .map(Article.ArticleId::getId).toList();
            log.info("Ids to clear deleted article: {}", ids);

            if (!ids.isEmpty()) {

                log.info("Clear private links");
                privateLinkRepo.deleteAllByArticleIdIsIn(ids);

                log.info("Clear reactions");
                reactionRepo.deleteAllByArticleIdIn(ids);

                log.info("Clear reports");
                reportRepo.deleteAllByArticleIdIn(ids);

                log.info("Clear articles");
                articleRepo.deleteAllByIdInBatch(ids);
            } else {
                log.info("No need to clear deleted article");
            }
        } catch (Exception ex) {
            log.error("Error clear deleted article", ex);
        }

        log.info("End to clear deleted article");
    }

    // TODO
    @Transactional
    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.HOURS)
    public void processArticle() {
        log.info("Start to process pending article");

        try {
            var pageRequest = PageRequest.of(0, 100);
            var page = articleRepo.findAllByStatus(List.of(Article.Status.PENDING.name()), pageRequest);

            if (!page.isEmpty()) {
                page.getContent().forEach(it -> {

                    try {
                        // TODO send to checker service and send email after checking
                        it.setStatus(Article.Status.PUBLISHED);
                        it.setPublishedDate(LocalDateTime.now());
                        articleRepo.save(it);
                    } catch (Exception ex) {
                        log.error("Error process article", ex);
                    }
                });
            }
        } catch (Exception ex) {
            log.error("Error process articles", ex);
        }

        log.info("End to process pending article");
    }

    @Transactional
    @Scheduled(fixedDelay = 2L, timeUnit = TimeUnit.HOURS)
    public void processReport() {
        log.info("Start to process report");
        var articleToSave = new ArrayList<Article>();

        reportRepo.countReport().forEach(it -> {

            if (it.getCount() >= maxReportCount) {
                articleRepo.findById(it.getId()).ifPresent(article -> {
                    article.setStatus(Article.Status.BLOCKED);
                    article.setPublishedDate(null);
                    articleToSave.add(article);
                });
            }
        });

        if (!articleToSave.isEmpty()) {
            log.info("Found {} articles to block", articleToSave.size());
            articleRepo.saveAll(articleToSave);

            var builder = new StringBuilder();
            articleToSave.forEach(it -> builder.append(it.getId()).append(", "));
//            reportRepo.deleteAllByArticleIdIn(articleToSave.stream().map(Article::getId).toList());
        }

        log.info("End to process report");
    }
}
