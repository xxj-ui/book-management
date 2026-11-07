package com.bookmgt.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器配置
 * 注册路由拦截规则，实现双 token 鉴权
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /** 白名单路由 — 不需要登录即可访问 */
    private static final String[] EXCLUDE_PATH = {
            "/auth/login",
            "/auth/sms-login",
            "/auth/sms-code",
            "/auth/refresh",
            "/doc.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/druid/**",
            "/favicon.ico"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 所有请求 — 排除白名单后校验登录
            SaRouter.match("/**")
                    .notMatch(EXCLUDE_PATH)
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
