package com.mvtalker.utilities.entity.community.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class JoinCommunityRequest
{
    @NotBlank(message = "字段 communityCode 不能为空")
    private String communityCode;
}
