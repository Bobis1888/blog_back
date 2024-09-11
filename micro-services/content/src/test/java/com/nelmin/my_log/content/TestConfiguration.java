package com.nelmin.my_log.content;

import com.nelmin.my_log.common.CommonConfiguration;
import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.handler.OAuthSuccessHandler;
import com.nelmin.my_log.common.model.User;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    @MockBean
    private User.Repo userRepo;

    @MockBean
    private CommonConfiguration commonConfiguration;

    @MockBean
    private ContentConfiguration contentConfiguration;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuthSuccessHandler oAuthSuccessHandler;

    @MockBean
    private TaskScheduler taskScheduler;

    @Bean
    public UserInfo userInfo() {
        var user = new User();
        user.setId(1L);
        user.setNickName("test");
        return new UserInfo(user);
    }
}
