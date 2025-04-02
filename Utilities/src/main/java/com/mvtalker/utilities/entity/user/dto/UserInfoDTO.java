package com.mvtalker.utilities.entity.user.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;


@Data
public class UserInfoDTO
{
    private Long userId;

    // 如此配置即可实现手机号在传输给前端之前进行遮蔽处理
    @JsonSerialize(using = MobileMaskSerializer.class)
    private String mobileEncrypted;

    private String nickname;
    private String avatarUrl;

    public UserInfoDTO(Long userId, String mobileEncrypted, String nickname, String avatarUrl)
    {
        this.userId = userId;
        this.mobileEncrypted = mobileEncrypted;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    static class MobileMaskSerializer extends JsonSerializer<String>
    {
        @Override
        public void serialize(String mobile, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            boolean isChinaPhone = mobile.startsWith("+86");

            if (isChinaPhone) {
                // 中国手机号的处理：保留国家代码 +86，保留前3位、遮蔽中间4位、保留后4位
                String prefix = mobile.substring(0, 3); // 国家代码 +86
                String mobileWithoutCountryCode = mobile.substring(3); // 去除国家码
                String phonePrefix = mobileWithoutCountryCode.substring(0, 3); // 前3位
                String phoneSuffix = mobileWithoutCountryCode.substring(mobileWithoutCountryCode.length() - 4); // 后4位
                String stars = "****"; // 遮蔽中间4位

                gen.writeString(prefix + phonePrefix + stars + phoneSuffix);
            }
            else
            {
                // 其他国家手机号的统一处理：保留前3位，遮蔽中间4位，保留后4位
                String prefix = mobile.substring(0, 3); // 前3位
                String suffix = mobile.substring(mobile.length() - 4); // 后4位
                int maskLength = mobile.length() - 7; // 总长度 - (3前缀 +4后缀)
                String stars = String.join("", Collections.nCopies(maskLength, "*"));

                gen.writeString(prefix + stars + suffix);
            }
        }
    }
}
