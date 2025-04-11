package com.mvtalker.utilities.entity.user.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mvtalker.utilities.common.StringToLongDeserializer;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserIdMultiRequest
{
    @NotNull(message = "字段 userIds 不能为空")
    @JsonDeserialize(contentUsing = StringToLongDeserializer.class)
    private List<Long> userIds;
}
