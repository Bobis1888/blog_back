package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.common.service.FillContentInfo;
import com.nelmin.my_log.common.service.FillStatisticInfo;
import com.nelmin.my_log.content.dto.ArticleDto;
import com.nelmin.my_log.content.dto.StatisticsResponseDto;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.model.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService implements FillContentInfo<ArticleDto>, FillStatisticInfo<StatisticsResponseDto> {

    private final UserInfo userInfo;
    private final Like.Repo likeRepo;
    private final Article.Repo articleRepo;

    @Transactional
    public SuccessDto like(Long id) {
        return changeLike(id, true);
    }

    @Transactional
    public SuccessDto dislike(Long id) {
        return changeLike(id, false);
    }

    @Transactional
    public Long countLike(Long articleId) {
        return likeRepo.countByArticleIdAndValue(articleId, true);
    }

    @Override
    @Transactional
    public void fillStatisticInfo(StatisticsResponseDto response) {
        var userId = response.getUserid();
        response.setLikes(likeRepo.countByUserId(userId));
    }

    @Override
    @Transactional
    public void fillContentInfo(ArticleDto response) {
        var userId = userInfo.getId();
        response.setLikes(countLike(response.getId()));
        likeRepo.getValueByArticleIdAndUserId(response.getId(), userId)
                .ifPresentOrElse(
                        (it) -> response.setIsLiked(it.getValue()),
                        () -> response.setIsLiked(false));
    }

    private SuccessDto changeLike(Long articleId, Boolean value) {
        var response = new SuccessDto(false);

        try {

            if (!articleRepo.existsById(articleId)) {
                response.reject("notFound", "article");
            } else {
                Long userid = userInfo.getId();

                if (value) {
                    Like like = likeRepo.findByArticleIdAndUserId(articleId, userid).orElse(new Like());
                    like.setUserId(userid);
                    like.setArticleId(articleId);
                    like.setValue(true);
                    likeRepo.save(like);
                    response.setSuccess(like.getId() != null);
                } else {
                    // TODO count dislike
                    likeRepo.deleteByArticleIdAndUserId(articleId, userid);
                    response.setSuccess(true);
                }
            }
        } catch (Exception ex) {
            log.error("Error save like", ex);
        }

        return response;
    }

}
