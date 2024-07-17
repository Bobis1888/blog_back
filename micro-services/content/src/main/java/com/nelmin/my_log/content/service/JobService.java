package com.nelmin.my_log.content.service;

import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.model.PrivateLink;
import com.nelmin.my_log.content.model.Reaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class JobService {

    private final Article.Repo articleRepo;
    private final Reaction.Repo reactionRepo;
    private final PrivateLink.Repo privateLinkRepo;

    @Transactional
    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.HOURS)
    public void clearDeletedArticle() {
        log.info("Start to clear deleted article");

        try {
            var pageRequest = PageRequest.of(0, 1000);
            var ids = articleRepo.getIdsByStatusIn(List.of(Article.Status.DELETED), pageRequest)
                    .map(Article.ArticleId::getId).toList();
            log.info("Ids to clear deleted article: {}", ids);

            log.info("Clear private links");
            privateLinkRepo.deleteAllByArticleIdIsIn(ids);

            log.info("Clear articles");
            articleRepo.deleteAllByIdInBatch(ids);

            log.info("Clear reactions");
            reactionRepo.deleteAllByArticleIdIn(ids);

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
}
