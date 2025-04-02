package com.mvtalker.utilities.entity.user.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserIdMultiRequest
{
    @NotNull(message = "字段 userIds 不能为空")
    private List<Long> userIds;
}
