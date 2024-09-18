package com.nelmin.my_log.content;

import com.nelmin.my_log.common.CommonConfiguration;
import com.nelmin.my_log.user_info.core.IUser;
import com.nelmin.my_log.user_info.core.UserInfo;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    @MockBean
    private CommonConfiguration commonConfiguration;

    @MockBean
    private ContentConfiguration contentConfiguration;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private TaskScheduler taskScheduler;

    @Bean
    public UserInfo userInfo() {
        var user = new IUser() {
            @Override
            public Long id() {
                return 1L;
            }

            @Override
            public String username() {
                return "test";
            }

            @Override
            public Boolean isEnabled() {
                return true;
            }

            @Override
            public Boolean isPremiumUser() {
                return false;
            }

            @Override
            public String nickname() {
                return "test";
            }

            @Override
            public Boolean isBlocked() {
                return false;
            }
        };

        return new UserInfo(user);
    }
}
