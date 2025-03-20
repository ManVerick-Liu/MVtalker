package com.mvtalker.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mvtalker.user.entity.po.DevicePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IDeviceMapper extends BaseMapper<DevicePO>
{
    default DevicePO selectByDeviceId(String deviceId)
    {
        return selectOne(new LambdaQueryWrapper<DevicePO>().eq(DevicePO::getDeviceId, deviceId));
    }
}
