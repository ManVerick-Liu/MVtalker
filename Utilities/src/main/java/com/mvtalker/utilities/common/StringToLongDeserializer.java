package com.mvtalker.utilities.common;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class StringToLongDeserializer extends JsonDeserializer<Long>
{
    @Override
    public Long deserialize(JsonParser p, DeserializationContext context) throws IOException
    {
        try
        {
            String value = p.getValueAsString();
            if (value == null || value.isEmpty()) {
                log.warn("空值无法转换为Long | path={}", p.getCurrentLocation());
                throw new JsonParseException(p, "空值不允许");
            }
            return Long.parseLong(value);
        }
        catch (NumberFormatException e)
        {
            log.error("数值转换失败 | input={} | error={}",
                    p.getText(),
                    e.getMessage()
            );
            throw new JsonParseException(p, "无效的Long格式: " + p.getText());
        }
    }
}
