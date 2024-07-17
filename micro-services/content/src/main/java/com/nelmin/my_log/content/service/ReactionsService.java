package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.common.service.FillContentInfo;
import com.nelmin.my_log.common.service.FillStatisticInfo;
import com.nelmin.my_log.content.dto.ArticleDto;
import com.nelmin.my_log.content.dto.StatisticsResponseDto;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.model.Reaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionsService implements FillContentInfo<ArticleDto>, FillStatisticInfo<StatisticsResponseDto> {

    private final UserInfo userInfo;
    private final Reaction.Repo reactionRepo;
    private final Article.Repo articleRepo;

    @Transactional
    public SuccessDto react(Long articleId, String type) {
        var response = new SuccessDto(false);

        try {

            if (!articleRepo.existsById(articleId)) {
                response.reject("notFound", "article");
            } else {
                Long userid = userInfo.getId();
                Reaction reaction = reactionRepo.findByArticleIdAndUserId(articleId, userid).orElse(new Reaction());
                reaction.setUserId(userid);
                reaction.setArticleId(articleId);
                reaction.setValue(type);
                reactionRepo.save(reaction);
                response.setSuccess(reaction.getId() != null);
            }
        } catch (Exception ex) {
            log.error("Error save reaction", ex);
        }

        return response;
    }

    @Transactional
    public SuccessDto remove(Long articleId) {
        var response = new SuccessDto(false);

        try {

            if (!articleRepo.existsById(articleId)) {
                response.reject("notFound", "article");
            } else {
                Long userid = userInfo.getId();
                reactionRepo.deleteByArticleIdAndUserId(articleId, userid);
                response.setSuccess(true);
            }
        } catch (Exception ex) {
            log.error("Error save reaction", ex);
        }

        return response;
    }

    @Transactional
    public List<Reaction.CountReaction> countReactions(Long articleId) {
        return reactionRepo.countByArticleId(articleId);
    }

    @Override
    @Transactional
    public void fillStatisticInfo(StatisticsResponseDto response) {
        var userId = response.getUserid();
        response.setReactions(reactionRepo.countByUserId(userId).stream().mapToLong(Reaction.CountReaction::getCount).sum());
    }

    @Override
    @Transactional
    public void fillContentInfo(ArticleDto response) {
        var userId = userInfo.getId();
        var userReaction = reactionRepo.getValueByArticleIdAndUserId(response.getId(), userId);

        response.setReactions(
                countReactions(response.getId())
                        .stream()
                        .map(it ->
                                new ArticleDto.ReactionDto(
                                        it.getCount(),
                                        it.getValue(),
                                        userReaction.isPresent() && userReaction
                                                .get()
                                                .getValue()
                                                .equals(it.getValue()))
                        )
                        .toList()
        );
    }
}
