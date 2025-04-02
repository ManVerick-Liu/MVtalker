package com.mvtalker.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mvtalker.user.entity.po.UserDevicePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserDeviceMapper extends BaseMapper<UserDevicePO>
{
    default UserDevicePO selectByDeviceId(String deviceId)
    {
        return selectOne(new LambdaQueryWrapper<UserDevicePO>().eq(UserDevicePO::getDeviceId, deviceId));
    }
}
