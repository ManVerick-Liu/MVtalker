package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.CommunityInfoDTO;
import lombok.Data;

import java.util.List;

@Data
public class CommunityInfoMultiResponse
{
    private List<CommunityInfoDTO> communityInfos;

    public CommunityInfoMultiResponse(List<CommunityInfoDTO> communityInfos)
    {
        this.communityInfos = communityInfos;
    }

    public CommunityInfoMultiResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
