package com.nelmin.my_log.common.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

@Slf4j
//@Component
//@RequiredArgsConstructor
public class CommonInterceptor implements HandlerInterceptor {

//    private final ApplicationContext context;
//    private final HttpSession session;

//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // TODO
////        request.setAttribute("REQUEST_ID", UUID.randomUUID().toString());
////        request.setAttribute("SESSION_ID", session.getId());
//
//        if (Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("dev")) {
//            response.setHeader("Access-Control-Allow-Credentials", "true");
//            response.setHeader("Access-Control-Allow-Origin", "*");
//            response.setHeader("Access-Control-Allow-Headers", "*");
//        }
//
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//
//    }
//
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//    }


}
