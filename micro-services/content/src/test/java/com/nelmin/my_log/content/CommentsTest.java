package com.nelmin.my_log.content;
import com.nelmin.my_log.content.dto.comment.CreateCommentRequestDto;
import com.nelmin.my_log.content.dto.kafka.ContentEvent;
import com.nelmin.my_log.content.model.Comment;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.service.CommentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = TestConfiguration.class
)
public class CommentsTest {

    @Autowired
    private CommentService commentService;

    @MockBean
    private Comment.Repo commentRepo;

    @MockBean
    private Article.Repo articleRepo;


    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void prepare() {
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(ContentEvent.class))).then(it -> {
            return new CompletableFuture<>();
        });
    }

    @Test
    @DisplayName("create content test")
    void createContentTest() {
        var req = new CreateCommentRequestDto("test", 2L, null);
        var response = commentService.save(req);

        Assertions.assertEquals(response.getSuccess(), Boolean.TRUE);
    }
}
