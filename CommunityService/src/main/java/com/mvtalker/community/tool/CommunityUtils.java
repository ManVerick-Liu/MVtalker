package com.mvtalker.community.tool;

import com.mvtalker.community.entity.po.ChatChannelPO;
import com.mvtalker.community.entity.po.CommunityInfoPO;
import com.mvtalker.community.entity.po.CommunityMemberPO;
import com.mvtalker.community.mapper.IChatChannelMapper;
import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.community.dto.*;
import com.mvtalker.utilities.entity.community.enums.MemberRole;
import com.mvtalker.utilities.entity.user.dto.UserInfoDTO;
import com.mvtalker.utilities.entity.user.dto.UserStatusDTO;
import com.mvtalker.utilities.entity.user.dto.UserViewDTO;
import com.mvtalker.utilities.entity.user.response.UserViewMultiResponse;
import com.mvtalker.utilities.exception.DataConsistencyException;
import com.mvtalker.utilities.exception.DataInconsistencyException;
import com.mvtalker.utilities.exception.FeignClientException;
import com.mvtalker.utilities.exception.PermissionDeniedException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityUtils
{
    public static void validateRoleUpdate(MemberRole operatorRole, MemberRole targetCurrentRole, MemberRole newRole) throws PermissionDeniedException
    {

        // 权限规则矩阵
        if (operatorRole == MemberRole.MEMBER)
        {
            throw new PermissionDeniedException("普通成员无权限修改角色");
        }

        if (operatorRole == MemberRole.ADMIN)
        {
            if (targetCurrentRole != MemberRole.MEMBER)
            {
                throw new PermissionDeniedException("管理员只能操作普通成员");
            }
            if (newRole != null)
            {
                throw new PermissionDeniedException("管理员无法修改成员角色");
            }
        }

        if (operatorRole == MemberRole.OWNER)
        {
            if (targetCurrentRole == MemberRole.OWNER)
            {
                throw new PermissionDeniedException("无法修改社区创建者角色");
            }
            if (newRole == MemberRole.OWNER)
            {
                throw new PermissionDeniedException("社区创建者身份不可转移");
            }
            if (targetCurrentRole == MemberRole.ADMIN && newRole == MemberRole.ADMIN)
            {
                throw new PermissionDeniedException("目标用户已是管理员");
            }
        }
    }

    public static CommunityInfoDTO buildCommunityInfoDTO(CommunityInfoPO communityInfoPO)
    {
        return new CommunityInfoDTO(
                communityInfoPO.getCommunityId(),
                communityInfoPO.getName(),
                communityInfoPO.getDescription(),
                communityInfoPO.getCreatorId(),
                communityInfoPO.getMaxMembers(),
                communityInfoPO.getVisibility(),
                communityInfoPO.getJoinValidation(),
                communityInfoPO.getCommunityCode(),
                communityInfoPO.getIconUrl()
        );
    }

    public static CommunityMemberDTO buildCommunityMemberDTO(CommunityMemberPO communityMemberPO)
    {
        return new CommunityMemberDTO(
                communityMemberPO.getCommunityId(),
                communityMemberPO.getUserId(),
                communityMemberPO.getRole()
        );
    }

    public static CommunityMemberViewDTO buildCommunityMemberViewDTO(CommunityMemberPO communityMemberPO, UserInfoDTO userInfoDTO, UserStatusDTO userStatusDTO)
    {
        return new CommunityMemberViewDTO(
                userInfoDTO.getUserId(),
                userInfoDTO.getNickname(),
                userInfoDTO.getAvatarUrl(),
                userStatusDTO.getOnlineStatus(),
                userStatusDTO.getAccountStatus(),
                userStatusDTO.getLastOnline(),
                communityMemberPO.getCommunityId(),
                communityMemberPO.getRole()
        );
    }

    public static CommunityMemberViewDTO buildCommunityMemberViewDTO(CommunityMemberPO communityMemberPO, UserViewDTO userViewDTO)
    {
        return new CommunityMemberViewDTO(
                userViewDTO.getUserId(),
                userViewDTO.getNickname(),
                userViewDTO.getAvatarUrl(),
                userViewDTO.getOnlineStatus(),
                userViewDTO.getAccountStatus(),
                userViewDTO.getLastOnline(),
                communityMemberPO.getCommunityId(),
                communityMemberPO.getRole()
        );
    }

    public static void validateMemberConsistency(List<CommunityMemberPO> members, List<UserViewDTO> userViews)
    {
        if (members.size() != userViews.size())
        {
            throw new DataInconsistencyException("数据量不匹配");
        }

        Set<Long> memberIds = members.stream()
                .map(CommunityMemberPO::getUserId)
                .collect(Collectors.toSet());

        Set<Long> viewIds = userViews.stream()
                .map(UserViewDTO::getUserId)
                .collect(Collectors.toSet());

        if (!memberIds.equals(viewIds))
        {
            throw new DataInconsistencyException("用户ID集合不一致");
        }
    }

    public static List<CommunityMemberViewDTO> mapToMemberViews(List<CommunityMemberPO> members, List<UserViewDTO> userViews)
    {
        Map<Long, CommunityMemberPO> memberMap = members.stream()
                .collect(Collectors.toMap(
                        CommunityMemberPO::getUserId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        return userViews.stream()
                .map(view -> {
                    CommunityMemberPO member = Optional.ofNullable(memberMap.get(view.getUserId()))
                            .orElseThrow(() -> new DataConsistencyException("用户[" + view.getUserId() + "]数据缺失"));
                    return CommunityUtils.buildCommunityMemberViewDTO(member, view);
                })
                .collect(Collectors.toList());
    }

    public static ChatChannelDTO buildChatChannelDTO(ChatChannelPO chatChannelPO)
    {
        return new ChatChannelDTO(
                chatChannelPO.getChatChannelId(),
                chatChannelPO.getCommunityId(),
                chatChannelPO.getName(),
                chatChannelPO.getCapacity()
        );
    }

    public static ChatChannelMemberDTO buildChatChannelMemberDTO(ChatChannelPO chatChannelPO)
    {
        return new ChatChannelMemberDTO(
                chatChannelPO.getChatChannelId(),
                chatChannelPO.getCommunityId()
        );
    }

    public static <T> void handleDatabaseError(BaseResponse<T> response, String message, Exception e) {
        log.error("数据库操作异常: {}", e.getMessage());
        response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        response.setMessage(message);
    }

    public static <T> void handleFeignError(BaseResponse<T> response, String message, FeignClientException e) {
        log.error("服务调用异常: {}", e.getMessage());
        response.setCode(HttpStatus.SC_BAD_GATEWAY);
        response.setMessage(message);
    }

    public static <T> void handleGenericError(BaseResponse<T> response, String message, Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        response.setMessage(message);
    }

    public static <T> BaseResponse<T> buildErrorResponse(BaseResponse<T> response, int code, String message) {
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
