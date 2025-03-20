package com.mvtalker.utilities.interceptor;

import cn.hutool.core.util.StrUtil;
import com.mvtalker.utilities.common.GlobalConstantValue;
import com.mvtalker.utilities.common.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

// 微服务接收来自网关的用户信息
// 在请求到达Controller前获取用户信息，在这里写可以避免代码重复
@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor
{
    @Override
    public boolean preHandle(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler) throws Exception
    {
        // 获取登录用户信息
        String userInfo = request.getHeader(GlobalConstantValue.USER_CONTEXT_ID_HEADER_NAME);

        // 校验
        if(StrUtil.isNotBlank(userInfo))
        {
            // 保存到用户上下文中，后面其他微服务就可以通过UserContext获取用户信息
            UserContext.setUserId(Long.valueOf(userInfo));
        }
        else
        {
            log.warn("未能从HTTP请求中获取用户信息");
        }
        // 放行
        return true;
    }

    @Override
    public void afterCompletion(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler, Exception ex) throws Exception
    {
        // 清理用户信息
        UserContext.removeUserId();
    }
}
