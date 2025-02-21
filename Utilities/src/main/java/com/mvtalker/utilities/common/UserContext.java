package com.mvtalker.utilities.common;

public class UserContext
{
    private static final ThreadLocal<String> userContextThreadLocal = new ThreadLocal<>();

    public static void setUserContext(String userContext)
    {
        userContextThreadLocal.set(userContext);
    }

    public static String getUserContext()
    {
        return userContextThreadLocal.get();
    }

    public static void removeUserContext()
    {
        userContextThreadLocal.remove();
    }
}
