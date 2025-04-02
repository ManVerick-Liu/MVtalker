package com.mvtalker.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mvtalker.user.entity.po.UserLocalVolumePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserLocalVolumeMapper extends BaseMapper<UserLocalVolumePO>
{
    default UserLocalVolumePO selectBySourceAndTarget(Long sourceId, Long targetId)
    {
        return selectOne(new LambdaQueryWrapper<UserLocalVolumePO>()
                .eq(UserLocalVolumePO::getSourceId, sourceId)
                .eq(UserLocalVolumePO::getTargetId, targetId));
    }
}
