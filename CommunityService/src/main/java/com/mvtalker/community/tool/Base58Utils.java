
package com.mvtalker.community.tool;

import org.springframework.stereotype.Component;

@Component
public class Base58Utils
{
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int BASE = ALPHABET.length;

    public static String encode(long input)
    {
        if (input == 0)
        {
            return String.valueOf(ALPHABET[0]);
        }
        StringBuilder result = new StringBuilder();
        while (input > 0)
        {
            int remainder = (int) (input % BASE);
            result.append(ALPHABET[remainder]);
            input = input / BASE;
        }
        return result.reverse().toString();
    }
}