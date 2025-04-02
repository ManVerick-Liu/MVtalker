package com.mvtalker.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mvtalker.community.entity.po.CommunityMemberPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ICommunityMemberMapper extends BaseMapper<CommunityMemberPO>
{
}
