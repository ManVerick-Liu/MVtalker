package com.mvtalker.utilities.interceptor;

import cn.hutool.core.util.StrUtil;
import com.mvtalker.utilities.common.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

// 微服务接收来自网关的用户信息
// 在请求到达Controller前获取用户信息，在这里写可以避免代码重复
public class UserInfoInterceptor implements HandlerInterceptor
{
    @Override
    public boolean preHandle(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler) throws Exception
    {
        // 获取登录用户信息
        String userInfo = String.valueOf(request.getHeaders("userInfo"));

        // 校验
        if(StrUtil.isNotBlank(userInfo))
        {
            UserContext.setUserContext(userInfo);
        }
        // 放行
        return true;
    }

    @Override
    public void afterCompletion(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler, Exception ex) throws Exception
    {
        // 清理用户信息
        UserContext.removeUserContext();
    }
}
