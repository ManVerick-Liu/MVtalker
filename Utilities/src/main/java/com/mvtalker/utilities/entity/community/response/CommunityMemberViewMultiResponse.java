package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.CommunityMemberViewDTO;
import lombok.Data;

import java.util.List;

@Data
public class CommunityMemberViewMultiResponse
{
    private List<CommunityMemberViewDTO> communityMemberViews;

    public CommunityMemberViewMultiResponse(List<CommunityMemberViewDTO> communityMemberViews)
    {
        this.communityMemberViews = communityMemberViews;
    }
    public CommunityMemberViewMultiResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
