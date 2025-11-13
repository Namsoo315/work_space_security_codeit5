package com.codeit.session.security;

import com.codeit.session.service.InitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminAndBlogInitializer implements ApplicationRunner {

    private final InitService initService;

    @Override
    public void run(ApplicationArguments args) {
        initService.initAdmin();
        initService.initDefaultUser();
        initService.initSamplePosts();
    }
}