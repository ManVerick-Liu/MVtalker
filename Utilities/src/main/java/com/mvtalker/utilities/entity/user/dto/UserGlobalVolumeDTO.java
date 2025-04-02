
package com.mvtalker.utilities.entity.user.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserGlobalVolumeDTO
{
    private Long userId;
    private Integer outputVolume;
    private Boolean outputActive;
    private Integer inputVolume;
    private Boolean inputActive;

    public UserGlobalVolumeDTO(Long userId, Integer outputVolume, Boolean outputActive, Integer inputVolume, Boolean inputActive)
    {
        this.userId = userId;
        this.outputVolume = outputVolume;
        this.outputActive = outputActive;
        this.inputVolume = inputVolume;
        this.inputActive = inputActive;
    }
}