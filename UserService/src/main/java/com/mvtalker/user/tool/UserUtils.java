package com.mvtalker.user.tool;

import com.mvtalker.user.entity.po.*;
import com.mvtalker.utilities.entity.user.dto.*;
import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import com.mvtalker.utilities.entity.user.enums.UserVisibility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UserUtils
{
    public static String maskMobile(String mobile)
    {
        if (mobile != null && mobile.matches("^\\+\\d{1,3}\\d{1,14}$") && mobile.length() >= 11)
        {
            String prefix = mobile.substring(0, 4);
            String suffix = mobile.substring(mobile.length() - 4);
            return prefix + "****" + suffix;
        }
        return mobile;
    }

    public static UserStatusDTO buildUserStatusDTO(UserStatusPO userStatusPO)
    {
        return new UserStatusDTO(
                userStatusPO.getUserId(),
                userStatusPO.getVisibility(),
                userStatusPO.getOnlineStatus(),
                userStatusPO.getAccountStatus(),
                userStatusPO.getLastOnline()
        );
    }

    public static UserInfoDTO buildUserInfoDTO(UserInfoPO userInfoPO)
    {
        return new UserInfoDTO(
                userInfoPO.getUserId(),
                userInfoPO.getMobileEncrypted(),
                userInfoPO.getNickname(),
                userInfoPO.getAvatarUrl()
        );
    }

    public static UserLocalVolumeDTO buildUserLocalVolumeDTO(UserLocalVolumePO userLocalVolumePO)
    {
        return new UserLocalVolumeDTO(
                userLocalVolumePO.getSourceId(),
                userLocalVolumePO.getTargetId(),
                userLocalVolumePO.getInputVolume(),
                userLocalVolumePO.getInputActive()
        );
    }

    public static UserGlobalVolumeDTO buildUserGlobalVolumeDTO(UserGlobalVolumePO userGlobalVolumePO)
    {
        return new UserGlobalVolumeDTO(
                userGlobalVolumePO.getUserId(),
                userGlobalVolumePO.getOutputVolume(),
                userGlobalVolumePO.getOutputActive(),
                userGlobalVolumePO.getInputVolume(),
                userGlobalVolumePO.getInputActive()
        );
    }

    public static UserViewDTO buildUserViewDTO(UserInfoPO userInfoPO, UserStatusPO userStatusPO)
    {
        if(userStatusPO.getVisibility() == UserVisibility.STEALTH)
        {
            return new UserViewDTO(
                    userInfoPO.getUserId(),
                    userInfoPO.getNickname(),
                    userInfoPO.getAvatarUrl(),
                    OnlineStatus.OFFLINE,
                    userStatusPO.getAccountStatus(),
                    userStatusPO.getLastOnline()
            );
        }

        return new UserViewDTO(
                userInfoPO.getUserId(),
                userInfoPO.getNickname(),
                userInfoPO.getAvatarUrl(),
                userStatusPO.getOnlineStatus(),
                userStatusPO.getAccountStatus(),
                userStatusPO.getLastOnline()
        );
    }

    public static List<UserViewDTO> buildUserViewDTOList(List<UserInfoPO> userInfoPOList, List<UserStatusPO> userStatusPOList)
    {
        if (userInfoPOList.size() != userStatusPOList.size())
        {
            throw new IllegalArgumentException("The size of userInfoDTOList and userStatusDTOList must be the same.");
        }

        List<UserViewDTO> userViewDTOList = new ArrayList<>();

        for (int i = 0; i < userInfoPOList.size(); i++)
        {
            UserInfoPO userInfoPO = userInfoPOList.get(i);
            UserStatusPO userStatusPO = userStatusPOList.get(i);

            UserViewDTO userViewDTO = buildUserViewDTO(userInfoPO, userStatusPO);

            userViewDTOList.add(userViewDTO);
        }

        return userViewDTOList;
    }

    // 手机号格式验证
    public static boolean isValidMobile(String mobile)
    {
        return mobile != null && mobile.matches("^\\+\\d{1,3}\\d{1,14}$");
    }

    // 密码强度校验
    public static boolean isPasswordStrong(String password)
    {
        return password.length() >= 8
                && password.matches(".*[A-Za-z].*")
                && password.matches(".*\\d.*");
    }
}
