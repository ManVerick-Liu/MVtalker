package com.mvtalker.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mvtalker.user.entity.po.UserPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserMapper extends BaseMapper<UserPO> // 继承即可获得17个基础CRUD方法，但是这些方法只能针对一张表
{
    default UserPO selectByMobile(String mobile)
    {
        return selectOne(Wrappers.<UserPO>lambdaQuery().eq(UserPO::getMobileEncrypted, mobile));
    }
}
