package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.CommunityInfoDTO;
import lombok.Data;

@Data
public class CommunityInfoResponse
{
    private CommunityInfoDTO communityInfo;

    public CommunityInfoResponse(CommunityInfoDTO communityInfo)
    {
        this.communityInfo = communityInfo;
    }

    public CommunityInfoResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
