package com.mvtalker.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mvtalker.community.entity.po.CommunityInfoPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ICommunityInfoMapper extends BaseMapper<CommunityInfoPO> // 继承即可获得17个基础CRUD方法，但是这些方法只能针对一张表
{

}
