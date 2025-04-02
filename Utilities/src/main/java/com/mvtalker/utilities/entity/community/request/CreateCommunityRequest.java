package com.mvtalker.utilities.entity.community.request;

import com.mvtalker.utilities.entity.community.enums.CommunityVisibility;
import lombok.Data;

@Data
public class CreateCommunityRequest
{
    private String name;
    private String description;
    private Integer maxMembers;
    private CommunityVisibility visibility;
    private Boolean joinValidation;
    private String iconUrl;
}
