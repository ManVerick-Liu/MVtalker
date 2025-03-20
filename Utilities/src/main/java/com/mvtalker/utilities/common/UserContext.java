package com.mvtalker.utilities.common;

public class UserContext
{
    // 要思考一下怎么在用户登录以及注册时把用户信息保存到线程上下文中
    private static final ThreadLocal<Long> userIdThreadLocal = new ThreadLocal<>();

    private static final ThreadLocal<String> userIpThreadLocal = new ThreadLocal<>();

    private static final ThreadLocal<String> deviceIdThreadLocal = new ThreadLocal<>();

    public static void setDeviceId(String userIp)
    {
        deviceIdThreadLocal.set(userIp);
    }

    public static String getDeviceId()
    {
        return deviceIdThreadLocal.get();
    }

    public static void setUserIp(String userIp)
    {
        userIpThreadLocal.set(userIp);
    }

    public static String getUserIp()
    {
        return userIpThreadLocal.get();
    }

    public static void setUserId(Long userId)
    {
        userIdThreadLocal.set(userId);
    }

    public static Long getUserId()
    {
        return userIdThreadLocal.get();
    }

    public static void removeUserId()
    {
        userIdThreadLocal.remove();
    }
}
