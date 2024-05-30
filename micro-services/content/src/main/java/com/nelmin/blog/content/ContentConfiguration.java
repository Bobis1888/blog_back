package com.nelmin.blog.content;

import com.nelmin.blog.common.abstracts.ProtectedPathsResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.nelmin.blog", considerNestedRepositories = true)
public class ContentConfiguration implements ProtectedPathsResolver {

    @Override
    public String[] getProtectedPaths() {
        return new String[]{"/save"};
    }
}
