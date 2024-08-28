package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.content.dto.kafka.UpdateImages;
import com.nelmin.my_log.content.model.Article;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentProcessorService {

    private final static String REGEX = "(?<=download\\?)type=TMP(&amp;|&)";
    private final static Pattern IMG_REGEX = Pattern.compile("(?<=download\\?uuid=).{0,36}");
    private final static Integer PREVIEW_LENGTH = 512;

    @Value("${content.events.topic:content-events}")
    private String eventsTopic;

    private final UserInfo userInfo;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Article.Repo articleRepo;

    public void process(@NonNull Article article, @NonNull String newContent) {
        newContent = newContent.replaceAll(REGEX, "");
        String oldContent = article.getContent();
        article.setContent(newContent);

        var generatedPreview = newContent.substring(0, Math.min(newContent.length(), PREVIEW_LENGTH));
        article.setPreView(generatedPreview.replaceAll(REGEX, ""));

        if (article.getId() == null) {
            return;
        }

        List<String> newImages = new ArrayList<>();
        List<String> removeImages = new ArrayList<>();

        var matcher = IMG_REGEX.matcher(newContent);

        while (matcher.find()) {
            newImages.add(matcher.group());
        }

        matcher = IMG_REGEX.matcher(oldContent);

        while (matcher.find()) {
            var image = matcher.group();

            if (!newImages.contains(image)) {
                removeImages.add(image);
            }
        }

        log.error("add images: {}, remove images: {}", newImages, removeImages);

        if (!newImages.isEmpty() || !removeImages.isEmpty()) {
            kafkaTemplate.send(eventsTopic, new UpdateImages(
                    userInfo.getId(),
                    newImages,
                    removeImages
            ));
        }
    }
}
