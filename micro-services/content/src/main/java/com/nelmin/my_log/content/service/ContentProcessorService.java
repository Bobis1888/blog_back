package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.content.dto.kafka.UpdateImages;
import com.nelmin.my_log.content.model.Article;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private static final String IMAGE_EVENTS = "image-events";

    private final UserInfo userInfo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void process(@NonNull Article article, @NonNull String newContent) {
        newContent = newContent.replaceAll(REGEX, "");
        String oldContent = article.getContent();
        article.setContent(newContent);

        var generatedPreview = newContent.substring(0, Math.min(newContent.length(), PREVIEW_LENGTH));
        article.setPreView(generatedPreview.replaceAll(REGEX, ""));

        List<String> newImages = new ArrayList<>();
        List<String> removeImages = new ArrayList<>();

        var matcher = IMG_REGEX.matcher(newContent);

        while (matcher.find()) {
            newImages.add(matcher.group());
        }


        if (StringUtils.hasText(oldContent)) {
            matcher = IMG_REGEX.matcher(oldContent);

            while (matcher.find()) {
                var image = matcher.group();

                if (!newImages.contains(image)) {
                    removeImages.add(image);
                }
            }
        }

        log.debug("add images: {}, remove images: {}", newImages, removeImages);

        if (!newImages.isEmpty() || !removeImages.isEmpty()) {
            kafkaTemplate.send(IMAGE_EVENTS, new UpdateImages(
                    userInfo.getId(),
                    newImages,
                    removeImages
            ));
        }
    }

    public void deleteImages(List<Article> list) {
        List<String> images = new ArrayList<>();

        list.forEach(it -> {
            var matcher = IMG_REGEX.matcher(it.getContent());

            while (matcher.find()) {
                images.add(matcher.group());
            }
        });

        if (!images.isEmpty()) {
            kafkaTemplate.send(IMAGE_EVENTS, new UpdateImages(
                    null,
                    null,
                    images
            ));
        }
    }
}
