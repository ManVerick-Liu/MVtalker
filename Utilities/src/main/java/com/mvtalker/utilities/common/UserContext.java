package com.mvtalker.utilities.common;

public class UserContext
{
    private static final ThreadLocal<Long> userIdThreadLocal = new ThreadLocal<>();

    // 目前弃用
    private static final ThreadLocal<String> userIpThreadLocal = new ThreadLocal<>();

    // 目前弃用
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

    public static void removeUserContext()
    {
        userIdThreadLocal.remove();
        userIpThreadLocal.remove();
        deviceIdThreadLocal.remove();
    }
}
