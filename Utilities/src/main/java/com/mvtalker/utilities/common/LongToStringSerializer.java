package com.mvtalker.utilities.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LongToStringSerializer extends JsonSerializer<Long>
{
    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        try
        {
            // 处理空值情况
            if (value == null)
            {
                if (serializers.getConfig().isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES))
                {
                    log.warn("序列化空值ID");
                    gen.writeNull();
                }
                else
                {
                    log.error("不允许的空值ID");
                    throw new IOException("ID字段不允许为null");
                }
                return;
            }

            // 核心转换逻辑
            gen.writeString(value.toString());

        }
        catch (IllegalArgumentException e)
        {
            log.error("序列化类型错误 | value={} | type={}", value, value != null ? value.getClass() : "null", e);
            throw new IOException("ID类型不合法", e);
        }
        catch (Exception e)
        {
            log.error("序列化异常 | value={}", value, e);
            throw new IOException("ID序列化失败", e);
        }
    }
}
