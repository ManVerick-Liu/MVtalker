package com.mvtalker.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mvtalker.user.entity.po.UserInfoPO;
import com.mvtalker.user.entity.po.UserStatusPO;
import com.mvtalker.user.mapper.IUserInfoMapper;
import com.mvtalker.user.mapper.IUserStatusMapper;
import com.mvtalker.user.service.interfaces.IUserTestService;
import com.mvtalker.user.tool.UserUtils;
import com.mvtalker.utilities.common.UserContext;
import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.user.dto.UserViewDTO;
import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import com.mvtalker.utilities.entity.user.enums.UserVisibility;
import com.mvtalker.utilities.entity.user.response.UserViewMultiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTestService implements IUserTestService
{
    private final UserUtils userUtils;
    private final IUserInfoMapper iUserInfoMapper;
    private final IUserStatusMapper iUserStatusMapper;

    @Override
    public BaseResponse<UserViewMultiResponse> getOnlineUserViewMulti()
    {
        BaseResponse<UserViewMultiResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 鉴权校验
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户未登录");
                return response;
            }

            // 2. 查询所有在线用户
            List<UserStatusPO> onlineUserStatusPOs = iUserStatusMapper.selectList(
                    new QueryWrapper<UserStatusPO>()
                            .eq("online_status", OnlineStatus.ONLINE)
                            .ne("visibility", UserVisibility.STEALTH)
            );
            if (onlineUserStatusPOs.isEmpty())
            {
                response.setCode(HttpStatus.SC_OK);
                response.setMessage("当前无在线用户");
                return response;
            }

            // 3. 提取在线用户ID集合
            List<Long> onlineUserIds = onlineUserStatusPOs.stream()
                    .map(UserStatusPO::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            List<UserInfoPO> onlineUserInfoPOs = iUserInfoMapper.selectBatchIds(onlineUserIds);

            // 4. 构建视图数据
            List<UserViewDTO> userViewDTOs = UserUtils.buildUserViewDTOList(onlineUserInfoPOs, onlineUserStatusPOs);
            UserViewMultiResponse userViewMultiResponse = new UserViewMultiResponse(userViewDTOs);

            response.setCode(HttpStatus.SC_OK);
            response.setData(userViewMultiResponse);
            response.setMessage("查询成功");
        }
        catch (DataAccessException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据查询失败");
        }
        catch (Exception e)
        {
            log.error("查询过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
        }

        return response;
    }
}
