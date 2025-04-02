package com.mvtalker.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mvtalker.user.entity.po.UserStatusPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserStatusMapper extends BaseMapper<UserStatusPO>
{
}
