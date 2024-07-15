package com.nelmin.my_log.content.service;

import com.nelmin.my_log.content.model.PrivateLink;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateLinkService {

    private final PrivateLink.Repo privateLinkRepo;

    @Transactional
    public String generate(Long articleId) {
        var privateLink = new PrivateLink();
        privateLink.setArticleId(articleId);
        privateLinkRepo.save(privateLink);
        return privateLink.getLink();
    }
}
