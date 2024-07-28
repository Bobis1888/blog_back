package com.nelmin.my_log.content.service;

import com.nelmin.my_log.content.dto.TagDto;
import com.nelmin.my_log.content.model.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author nelmin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final Article.Repo contentRepo;

    @Transactional
    // TODO redis cache
//    @Cacheable(value = "tags", key = "#query")
    public List<TagDto> list(Integer limit, String query) {
        try {
            return contentRepo.extractTags(limit, query)
                    .stream()
                    .map(it -> new TagDto(it.getTag(), it.getCount()))
                    .toList();
        } catch (Exception ex) {
            log.error("Error get tags", ex);
        }

        return List.of();
    }
}
