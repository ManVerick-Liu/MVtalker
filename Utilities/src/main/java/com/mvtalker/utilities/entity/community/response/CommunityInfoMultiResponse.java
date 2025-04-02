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
}
