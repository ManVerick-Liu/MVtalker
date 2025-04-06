package com.mvtalker.community.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mvtalker.community.entity.po.ChatChannelMemberPO;
import com.mvtalker.community.entity.po.ChatChannelPO;
import com.mvtalker.community.entity.po.CommunityInfoPO;
import com.mvtalker.community.entity.po.CommunityMemberPO;
import com.mvtalker.community.mapper.IChatChannelMapper;
import com.mvtalker.community.mapper.IChatChannelMemberMapper;
import com.mvtalker.community.mapper.ICommunityInfoMapper;
import com.mvtalker.community.mapper.ICommunityMemberMapper;
import com.mvtalker.community.service.interfaces.ICommunityService;
import com.mvtalker.community.tool.Base58Utils;
import com.mvtalker.community.tool.CommunityUtils;
import com.mvtalker.utilities.common.UserContext;
import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.community.dto.ChatChannelAndMemberMultiDTO;
import com.mvtalker.utilities.entity.community.dto.ChatChannelDTO;
import com.mvtalker.utilities.entity.community.dto.CommunityInfoDTO;
import com.mvtalker.utilities.entity.community.dto.CommunityMemberViewDTO;
import com.mvtalker.utilities.entity.community.enums.MemberRole;
import com.mvtalker.utilities.entity.community.request.*;
import com.mvtalker.utilities.entity.community.response.*;
import com.mvtalker.utilities.entity.user.dto.UserViewDTO;
import com.mvtalker.utilities.entity.user.request.UserIdMultiRequest;
import com.mvtalker.utilities.entity.user.response.UserViewMultiResponse;
import com.mvtalker.utilities.entity.user.response.UserViewResponse;
import com.mvtalker.utilities.exception.FeignClientException;
import com.mvtalker.utilities.exception.PermissionDeniedException;
import com.mvtalker.utilities.feign.UserFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService implements ICommunityService
{
    private final ICommunityInfoMapper iCommunityInfoMapper;
    private final ICommunityMemberMapper iCommunityMemberMapper;
    private final IChatChannelMapper iChatChannelMapper;
    private final IChatChannelMemberMapper iChatChannelMemberMapper;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<CommunityInfoAndMemberViewMultiResponse> createCommunity(CreateCommunityRequest createCommunityRequest)
    {
        BaseResponse<CommunityInfoAndMemberViewMultiResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("社区创建失败：用户未登录");
                log.error("社区创建失败：用户未登录");
                return response;
            }

            // 1. 构建并插入社区信息
            CommunityInfoPO communityInfoPO = new CommunityInfoPO();
            communityInfoPO.setName(createCommunityRequest.getName());
            communityInfoPO.setDescription(createCommunityRequest.getDescription());
            communityInfoPO.setCreatorId(UserContext.getUserId());
            communityInfoPO.setMaxMembers(createCommunityRequest.getMaxMembers());
            communityInfoPO.setVisibility(createCommunityRequest.getVisibility());
            communityInfoPO.setJoinValidation(createCommunityRequest.getJoinValidation());
            communityInfoPO.setIconUrl(createCommunityRequest.getIconUrl());

            if (iCommunityInfoMapper.insert(communityInfoPO) != 1)
            {
                throw new DataAccessException("社区信息数据插入失败") {};
            }

            // 2. 生成并持久化社区代码
            String communityCode = Base58Utils.encode(communityInfoPO.getCommunityId());
            communityInfoPO.setCommunityCode(communityCode);
            if (iCommunityInfoMapper.updateById(communityInfoPO) != 1)
            {
                throw new DataAccessException("社区代码更新失败") {};
            }

            // 3.添加社区创建者
            CommunityMemberPO creator = new CommunityMemberPO();
            creator.setCommunityId(communityInfoPO.getCommunityId());
            creator.setUserId(UserContext.getUserId());
            creator.setRole(MemberRole.OWNER);
            creator.setJoinTime(LocalDateTime.now());
            if (iCommunityMemberMapper.insert(creator) != 1)
            {
                throw new DataAccessException("社区成员添加失败") {};
            }

            // 4. 获取社区成员
            List<CommunityMemberPO> communityMemberPOs = iCommunityMemberMapper.selectList(new QueryWrapper<CommunityMemberPO>().eq("community_id", communityInfoPO.getCommunityId()));

            // 5. 成员列表空值校验（至少应包含创建者自己）
            if (communityMemberPOs == null || communityMemberPOs.isEmpty() || communityMemberPOs.stream().noneMatch(communityMemberPO -> communityMemberPO.getUserId().equals(UserContext.getUserId())))
            {
                log.error("社区[{}]成员数据异常，创建者未正确添加", communityInfoPO.getCommunityId());
                throw new IllegalStateException("社区成员数据异常");
            }

            // 6. Feign调用获取用户信息
            List<UserViewDTO> userViews;
            try
            {
                UserIdMultiRequest userIdMultiRequest = new UserIdMultiRequest();
                userIdMultiRequest.setUserIds(communityMemberPOs.stream().map(CommunityMemberPO::getUserId).collect(Collectors.toList()));

                ResponseEntity<BaseResponse<UserViewMultiResponse>> feignResponse = userFeignClient.getUserViewMultiByUserIdMulti(userIdMultiRequest);

                if (feignResponse == null)
                {
                    throw new FeignClientException("用户服务无响应");
                }

                if (feignResponse.getStatusCodeValue() != HttpStatus.SC_OK)
                {
                    throw new FeignClientException("用户服务返回非200状态码: " + feignResponse.getStatusCodeValue());
                }
                if (feignResponse.getBody() == null)
                {
                    throw new FeignClientException("用户服务返回空响应体");
                }
                BaseResponse<UserViewMultiResponse> body = feignResponse.getBody();
                if (body.getData() == null || body.getData().getUserViews() == null)
                {
                    String errorMsg = StringUtils.hasText(body.getMessage()) ?
                            body.getMessage() : "用户数据获取失败";
                    throw new FeignClientException(errorMsg);
                }

                userViews = feignResponse.getBody().getData().getUserViews();
            }
            catch (FeignException e)
            {
                log.error("Feign通信失败 status={}, reason={}", e.status(), e.getMessage());
                throw new FeignClientException("用户服务通信失败", e);
            }

            // 7. 构建响应数据
            CommunityInfoDTO communityInfoDto = CommunityUtils.buildCommunityInfoDTO(
                    iCommunityInfoMapper.selectById(communityInfoPO.getCommunityId()));
            CommunityUtils.validateMemberConsistency(communityMemberPOs, userViews);
            List<CommunityMemberViewDTO> communityMemberViews = CommunityUtils.mapToMemberViews(communityMemberPOs, userViews);

            CommunityInfoAndMemberViewMultiResponse responseData = new CommunityInfoAndMemberViewMultiResponse(communityInfoDto, communityMemberViews);

            response.setCode(HttpStatus.SC_CREATED);
            response.setMessage("社区创建成功");
            response.setData(responseData);

        }
        catch (DuplicateKeyException e)
        {
            log.error("唯一性冲突: {}", e.getMessage());
            response.setCode(HttpStatus.SC_CONFLICT);
            response.setMessage("数据冲突，请检查输入内容");
            return response;
        }
        catch (DataAccessException | IllegalStateException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据创建失败");
            return response;
        }
        catch (FeignClientException e)
        {
            log.error("服务调用异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_BAD_GATEWAY);
            response.setMessage("远程调用服务暂不可用");
            return response;
        }
        catch (Exception e)
        {
            log.error("创建过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
            return response;
        }

        return response;
    }

    @Override
    public BaseResponse<CommunityInfoResponse> joinCommunity(JoinCommunityRequest joinCommunityRequest)
    {
        BaseResponse<CommunityInfoResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 用户鉴权校验
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("加入社区失败：用户未登录");
                log.error("加入社区失败：用户未登录");
                return response;
            }

            // 2. 获取并校验社区信息
            CommunityInfoPO communityInfoPO = iCommunityInfoMapper.selectOne(
                    new QueryWrapper<CommunityInfoPO>()
                            .eq("community_code", joinCommunityRequest.getCommunityCode())
            );
            if(communityInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("加入社区失败：社区不存在");
                log.error("加入社区失败：社区不存在");
                return response;
            }

            // TODO: 3. 校验用户是否已经在社区中

            // TODO: 4. 处理需要管理员验证的情况
            if(communityInfoPO.getJoinValidation())
            {

            }

            // 5. 更新社区成员列表
            CommunityMemberPO communityMemberPO = new CommunityMemberPO();
            communityMemberPO.setCommunityId(communityInfoPO.getCommunityId());
            communityMemberPO.setUserId(UserContext.getUserId());
            communityMemberPO.setRole(MemberRole.MEMBER);
            iCommunityMemberMapper.insert(communityMemberPO);

            // 6. 构建响应数据
            CommunityInfoDTO communityInfoDto = CommunityUtils.buildCommunityInfoDTO(communityInfoPO);

            CommunityInfoResponse responseData = new CommunityInfoResponse(communityInfoDto);

            response.setCode(HttpStatus.SC_CREATED);
            response.setMessage("加入社区成功");
            response.setData(responseData);
        }
        catch (DuplicateKeyException e)
        {
            log.error("唯一性冲突: {}", e.getMessage());
            response.setCode(HttpStatus.SC_CONFLICT);
            response.setMessage("数据冲突，请检查输入内容");
            return response;
        }
        catch (DataAccessException | IllegalStateException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据创建失败");
            return response;
        }
        catch (Exception e)
        {
            log.error("创建过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
            return response;
        }

        return response;
    }

    @Override
    public BaseResponse<Void> dismissCommunity(Long communityId)
    {
        BaseResponse<Void> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 用户鉴权校验
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("解散社区失败：用户未登录");
                log.error("解散社区失败：用户未登录");
                return response;
            }

            // 2. 获取并校验社区信息
            CommunityInfoPO communityInfoPO = iCommunityInfoMapper.selectById(communityId);
            if(communityInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("解散社区失败：社区不存在");
                log.error("解散社区失败：社区不存在");
                return response;
            }

            // 3. 校验用户是否为社区创建者
            if(!Objects.equals(communityInfoPO.getCreatorId(), UserContext.getUserId()))
            {
                response.setCode(HttpStatus.SC_FORBIDDEN);
                response.setMessage("用户无权限解散社区：用户不是该社区的所有者");
                log.error("用户无权限解散社区：用户不是该社区的所有者");
                return response;
            }

            // 4. 解散社区，级联删除其他相关数据
            iCommunityInfoMapper.deleteById(communityId);

            // 5. 构建响应数据
            response.setCode(HttpStatus.SC_OK);
            response.setMessage("解散社区成功");
        }
        catch (DuplicateKeyException e)
        {
            log.error("唯一性冲突: {}", e.getMessage());
            response.setCode(HttpStatus.SC_CONFLICT);
            response.setMessage("数据冲突，请检查输入内容");
            return response;
        }
        catch (DataAccessException | IllegalStateException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据删除失败");
            return response;
        }
        catch (Exception e)
        {
            log.error("创建过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
            return response;
        }

        return response;
    }

    @Override
    public BaseResponse<CommunityInfoResponse> updateCommunityInfo(UpdateCommunityInfoRequest updateCommunityInfoRequest)
    {
        BaseResponse<CommunityInfoResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 用户鉴权校验
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                log.error("更新社区信息失败：用户未登录");
                return response;
            }

            // 2. 获取并校验社区信息
            CommunityInfoPO communityInfoPO = iCommunityInfoMapper.selectById(updateCommunityInfoRequest.getCommunityId());
            if(communityInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("社区不存在");
                log.error("更新社区信息失败：社区不存在");
                return response;
            }

            // 3. 校验用户是否在该社区中
            CommunityMemberPO communityMemberPO = iCommunityMemberMapper.selectOne(
                    new QueryWrapper<CommunityMemberPO>()
                            .eq("community_id", updateCommunityInfoRequest.getCommunityId())
                            .eq("user_id", UserContext.getUserId()));
            if(communityMemberPO == null)
            {
                response.setCode(HttpStatus.SC_FORBIDDEN);
                response.setMessage("用户无权限更新社区信息");
                log.error("更新社区信息失败：用户不是该社区的成员");
                return response;
            }

            // 4. 校验用户在该社区中的权限
            if(communityMemberPO.getRole() == MemberRole.MEMBER)
            {
                response.setCode(HttpStatus.SC_FORBIDDEN);
                response.setMessage("用户无权限更新社区信息");
                log.error("更新社区信息失败：用户不是该社区的管理员或创建者");
                return response;
            }

            // 5. 更新社区信息
            communityInfoPO.setName(updateCommunityInfoRequest.getName());
            communityInfoPO.setDescription(updateCommunityInfoRequest.getDescription());
            communityInfoPO.setMaxMembers(updateCommunityInfoRequest.getMaxMembers());
            communityInfoPO.setVisibility(updateCommunityInfoRequest.getVisibility());
            communityInfoPO.setJoinValidation(updateCommunityInfoRequest.getJoinValidation());
            communityInfoPO.setIconUrl(updateCommunityInfoRequest.getIconUrl());
            if(iCommunityInfoMapper.updateById(communityInfoPO) != 1)
            {
                response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                response.setMessage("更新社区信息失败");
                log.error("更新社区信息失败");
                return response;
            }

            // 6. 构建响应数据
            CommunityInfoPO communityInfo = iCommunityInfoMapper.selectById(updateCommunityInfoRequest.getCommunityId());
            CommunityInfoResponse communityInfoResponse = new CommunityInfoResponse(CommunityUtils.buildCommunityInfoDTO(communityInfo));
            response.setData(communityInfoResponse);
            response.setCode(HttpStatus.SC_OK);
            response.setMessage("更新社区信息成功");
        }
        catch (DuplicateKeyException e)
        {
            log.error("唯一性冲突: {}", e.getMessage());
            response.setCode(HttpStatus.SC_CONFLICT);
            response.setMessage("数据冲突，请检查输入内容");
            return response;
        }
        catch (DataAccessException | IllegalStateException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据删除失败");
            return response;
        }
        catch (Exception e)
        {
            log.error("创建过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
            return response;
        }

        return response;
    }

    @Override
    public BaseResponse<CommunityMemberViewResponse> updateCommunityMemberRole(UpdateCommunityMemberRoleRequest updateCommunityMemberRoleRequest)
    {
        BaseResponse<CommunityMemberViewResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 用户鉴权校验
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户未登录");
                log.error("更新社区用户权限失败：用户未登录");
                return response;
            }

            // 2. 获取并校验社区信息
            CommunityInfoPO communityInfoPO = iCommunityInfoMapper.selectById(updateCommunityMemberRoleRequest.getCommunityId());
            if(communityInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("社区不存在");
                log.error("更新社区用户权限失败：社区不存在");
                return response;
            }

            // 3. 校验操作者用户是否在该社区中
            CommunityMemberPO sourceUser = iCommunityMemberMapper.selectOne(
                    new QueryWrapper<CommunityMemberPO>()
                            .eq("community_id", updateCommunityMemberRoleRequest.getCommunityId())
                            .eq("user_id", UserContext.getUserId()));
            if(sourceUser == null)
            {
                response.setCode(HttpStatus.SC_FORBIDDEN);
                response.setMessage("操作者不是该社区的成员");
                log.error("更新社区用户权限失败：操作者不是该社区的成员");
                return response;
            }

            // 4. 校验被操作者是否在该社区中
            CommunityMemberPO targetUser = iCommunityMemberMapper.selectOne(
                    new QueryWrapper<CommunityMemberPO>()
                            .eq("community_id", updateCommunityMemberRoleRequest.getCommunityId())
                            .eq("user_id", updateCommunityMemberRoleRequest.getUserId()));
            if(targetUser == null)
            {
                response.setCode(HttpStatus.SC_FORBIDDEN);
                response.setMessage("被操作者不是该社区的成员");
                log.error("更新社区用户权限失败：被操作者不是该社区的成员");
                return response;
            }

            // 4. 权限校验矩阵
            CommunityUtils.validateRoleUpdate(
                    sourceUser.getRole(),
                    targetUser.getRole(),
                    updateCommunityMemberRoleRequest.getRole()
            );

            // 5. 执行用户权限修改
            targetUser.setRole(updateCommunityMemberRoleRequest.getRole());
            if (iCommunityMemberMapper.updateById(targetUser) != 1)
            {
                throw new DataAccessException("用户权限更新失败") {};
            }

            // 6. 构建响应数据
            UserViewDTO targetUserView;
            try
            {
                ResponseEntity<BaseResponse<UserViewResponse>> feignResponse = userFeignClient.getUserViewByUserId(targetUser.getUserId());

                if (feignResponse == null)
                {
                    throw new FeignClientException("用户服务无响应");
                }

                if (feignResponse.getStatusCodeValue() != HttpStatus.SC_OK)
                {
                    throw new FeignClientException("用户服务返回非200状态码: " + feignResponse.getStatusCodeValue());
                }

                if (feignResponse.getBody() == null)
                {
                    String errorMsg = feignResponse.getBody() != null ?
                            feignResponse.getBody().getMessage() : "响应体为空";
                    throw new FeignClientException("用户服务业务异常: " + errorMsg);
                }

                targetUserView = feignResponse.getBody().getData().getUserView();

            }
            catch (FeignException e)
            {
                log.error("Feign通信失败 status={}, reason={}", e.status(), e.getMessage());
                throw new FeignClientException("用户服务通信失败", e);
            }

            CommunityMemberViewDTO communityMemberView = CommunityUtils.buildCommunityMemberViewDTO(targetUser, targetUserView);
            CommunityMemberViewResponse communityMemberViewResponse = new CommunityMemberViewResponse(communityMemberView);

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("更新社区信息成功");
            response.setData(communityMemberViewResponse);
        }
        catch (PermissionDeniedException e)
        {
            log.error("权限校验失败: {}", e.getMessage());
            response.setCode(HttpStatus.SC_FORBIDDEN);
            response.setMessage(e.getMessage());
            return response;
        }
        catch (DuplicateKeyException e)
        {
            log.error("唯一性冲突: {}", e.getMessage());
            response.setCode(HttpStatus.SC_CONFLICT);
            response.setMessage("数据冲突，请检查输入内容");
            return response;
        }
        catch (DataAccessException | IllegalStateException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据删除失败");
            return response;
        }
        catch (FeignClientException e)
        {
            log.error("服务调用异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_BAD_GATEWAY);
            response.setMessage("远程调用服务暂不可用");
            return response;
        }
        catch (Exception e)
        {
            log.error("创建过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
            return response;
        }

        return response;
    }

    @Override
    public BaseResponse<CommunityEntireInfoResponse> getCommunityEntireInfoByCommunityId(Long communityId)
    {
        BaseResponse<CommunityEntireInfoResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 用户鉴权校验
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户未登录");
                log.error("用户未登录");
                return response;
            }

            // 2. 社区存在性校验
            CommunityInfoPO communityInfoPO = iCommunityInfoMapper.selectById(communityId);
            if (communityInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("社区数据不存在");
                log.error("社区数据不存在");
                return response;
            }

            // 3. 社区成员校验
            List<CommunityMemberPO> communityMemberPOs = iCommunityMemberMapper.selectList(new QueryWrapper<CommunityMemberPO>().eq("community_id", communityId));
            List<Long> userIds = communityMemberPOs.stream().map(CommunityMemberPO::getUserId).collect(Collectors.toList());
            if (!userIds.contains(UserContext.getUserId()))
            {
                response.setCode(HttpStatus.SC_FORBIDDEN);
                response.setMessage("用户无权限访问");
                log.error("用户无权限访问");
                return response;
            }

            // 4. 构建社区成员视图
            List<UserViewDTO> userViews;
            try
            {
                UserIdMultiRequest userIdMultiRequest = new UserIdMultiRequest();
                userIdMultiRequest.setUserIds(communityMemberPOs.stream().map(CommunityMemberPO::getUserId).collect(Collectors.toList()));
                ResponseEntity<BaseResponse<UserViewMultiResponse>> feignResponse =
                        userFeignClient.getUserViewMultiByUserIdMulti(userIdMultiRequest);


                if (feignResponse == null)
                {
                    throw new FeignClientException("用户服务无响应");
                }

                if (feignResponse.getStatusCodeValue() != HttpStatus.SC_OK)
                {
                    throw new FeignClientException("用户服务返回非200状态码: " + feignResponse.getStatusCodeValue());
                }

                if (feignResponse.getBody() == null)
                {
                    String errorMsg = feignResponse.getBody() != null ?
                            feignResponse.getBody().getMessage() : "响应体为空";
                    throw new FeignClientException("用户服务业务异常: " + errorMsg);
                }

                userViews = feignResponse.getBody().getData().getUserViews();

            }
            catch (FeignException e)
            {
                log.error("Feign通信失败 status={}, reason={}", e.status(), e.getMessage());
                throw new FeignClientException("用户服务通信失败", e);
            }
            CommunityUtils.validateMemberConsistency(communityMemberPOs, userViews);
            List<CommunityMemberViewDTO> communityMemberViews = CommunityUtils.mapToMemberViews(communityMemberPOs, userViews);

            // 5. 获取语音频道数据（带成员信息）
            List<ChatChannelAndMemberMultiDTO> chatChannels = buildChatChannelsWithMembers(communityId);

            // 6. 构建响应数据
            CommunityEntireInfoResponse responseData = new CommunityEntireInfoResponse(
                    CommunityUtils.buildCommunityInfoDTO(communityInfoPO),
                    communityMemberViews,
                    chatChannels
            );

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("社区全部数据获取成功");
            response.setData(responseData);

        }
        catch (DuplicateKeyException e)
        {
            log.error("唯一性冲突: {}", e.getMessage());
            response.setCode(HttpStatus.SC_CONFLICT);
            response.setMessage("数据冲突，请检查输入内容");
            return response;
        }
        catch (DataAccessException | IllegalStateException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据获取失败");
            return response;
        }
        catch (FeignClientException e)
        {
            log.error("服务调用异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_BAD_GATEWAY);
            response.setMessage("远程调用服务暂不可用");
            return response;
        }
        catch (Exception e)
        {
            log.error("创建过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
            return response;
        }

        return response;
    }

    @Override
    public BaseResponse<CommunityInfoMultiResponse> getCommunityInfosByUserId()
    {
        BaseResponse<CommunityInfoMultiResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 用户鉴权
            if (UserContext.getUserId() == null) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_UNAUTHORIZED, "用户未登录");
            }

            // 2. 查询用户加入的社区
            List<CommunityMemberPO> memberships = iCommunityMemberMapper.selectList(
                    new QueryWrapper<CommunityMemberPO>()
                            .eq("user_id", UserContext.getUserId())
            );

            // 3. 获取社区基本信息
            List<Long> communityIds = memberships.stream()
                    .map(CommunityMemberPO::getCommunityId)
                    .distinct()
                    .collect(Collectors.toList());

            List<CommunityInfoPO> communities = communityIds.isEmpty() ?
                    Collections.emptyList() :
                    iCommunityInfoMapper.selectBatchIds(communityIds);

            // 4. 构建响应数据
            List<CommunityInfoDTO> dtos = communities.stream()
                    .map(CommunityUtils::buildCommunityInfoDTO)
                    .collect(Collectors.toList());

            response.setData(new CommunityInfoMultiResponse(dtos));
            response.setCode(HttpStatus.SC_OK);
            response.setMessage("成功获取用户关联的社区信息");

        }
        catch (DataAccessException e)
        {
            CommunityUtils.handleDatabaseError(response, "查询社区信息失败", e);
        }
        catch (Exception e)
        {
            CommunityUtils.handleGenericError(response, "获取社区信息时发生未知错误", e);
        }
        return response;
    }

    @Override
    public BaseResponse<CommunityInfoResponse> getCommunityInfoByCommunityId(Long communityId)
    {
        BaseResponse<CommunityInfoResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 参数校验
            if (communityId == null) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "社区ID不能为空");
            }

            // 2. 查询社区信息
            CommunityInfoPO community = iCommunityInfoMapper.selectById(communityId);
            if (community == null) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_NOT_FOUND, "社区不存在");
            }

            // 3. 构建响应
            CommunityInfoDTO dto = CommunityUtils.buildCommunityInfoDTO(community);
            response.setData(new CommunityInfoResponse(dto));
            response.setCode(HttpStatus.SC_OK);
            response.setMessage("成功获取社区信息");

        } catch (DataAccessException e) {
            CommunityUtils.handleDatabaseError(response, "查询社区信息失败", e);
        } catch (Exception e) {
            CommunityUtils.handleGenericError(response, "获取社区信息时发生未知错误", e);
        }
        return response;
    }

    @Override
    public BaseResponse<CommunityMemberViewMultiResponse> getCommunityMembersByCommunityId(Long communityId)
    {
        BaseResponse<CommunityMemberViewMultiResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 参数校验
            if (communityId == null) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "社区ID不能为空");
            }

            // 2. 权限校验
            if (!isCommunityMember(communityId, UserContext.getUserId())) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_FORBIDDEN, "无权限访问该社区成员信息");
            }

            // 3. 获取成员列表
            List<CommunityMemberPO> members = iCommunityMemberMapper.selectList(
                    new QueryWrapper<CommunityMemberPO>()
                            .eq("community_id", communityId)
            );

            // 4. 获取用户信息
            List<Long> userIds = members.stream()
                    .map(CommunityMemberPO::getUserId)
                    .collect(Collectors.toList());

            List<UserViewDTO> userViews = new ArrayList<>(getUserViewMap(userIds).values());

            // 5. 构建响应
            List<CommunityMemberViewDTO> memberViews = CommunityUtils.mapToMemberViews(members, userViews);
            response.setData(new CommunityMemberViewMultiResponse(memberViews));
            response.setCode(HttpStatus.SC_OK);
            response.setMessage("成功获取社区成员列表");

        } catch (DataAccessException e) {
            CommunityUtils.handleDatabaseError(response, "查询成员信息失败", e);
        } catch (FeignClientException e) {
            CommunityUtils.handleFeignError(response, "获取用户信息失败", e);
        } catch (Exception e) {
            CommunityUtils.handleGenericError(response, "获取成员列表时发生未知错误", e);
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<ChatChannelResponse> createChatChannel(CreateChatChannelRequest createChatChannelRequest)
    {
        BaseResponse<ChatChannelResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 权限校验
            CommunityMemberPO member = iCommunityMemberMapper.selectOne(
                    new QueryWrapper<CommunityMemberPO>()
                            .eq("community_id", createChatChannelRequest.getCommunityId())
                            .eq("user_id", UserContext.getUserId())
            );
            if (member == null)
            {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_FORBIDDEN, "无权限创建频道");
            }
            if (member.getRole() != MemberRole.OWNER && member.getRole() != MemberRole.ADMIN)
            {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_FORBIDDEN, "无权限创建频道");
            }

            // 2. 创建频道
            ChatChannelPO channel = new ChatChannelPO();
            channel.setCommunityId(createChatChannelRequest.getCommunityId());
            channel.setName(createChatChannelRequest.getName());
            channel.setCapacity(createChatChannelRequest.getCapacity());
            if (iChatChannelMapper.insert(channel) != 1)
            {
                throw new DataAccessException("频道创建失败") {};
            }

            // TODO: WebRTC 频道创建

            // 3. 构建响应
            ChatChannelDTO dto = CommunityUtils.buildChatChannelDTO(channel);
            response.setData(new ChatChannelResponse(dto));
            response.setCode(HttpStatus.SC_CREATED);
            response.setMessage("频道创建成功");

        } catch (DataAccessException e) {
            CommunityUtils.handleDatabaseError(response, "频道创建失败", e);
        } catch (Exception e) {
            CommunityUtils.handleGenericError(response, "创建频道时发生未知错误", e);
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<ChatChannelAndMemberMultiResponse> joinChatChannel(ChatChannelIdRequest request)
    {
        BaseResponse<ChatChannelAndMemberMultiResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 参数校验
            if (request.getChatChannelId() == null || request.getCommunityId() == null) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "参数不完整");
            }

            // 2. 验证用户社区成员身份
            if (!isCommunityMember(request.getCommunityId(), UserContext.getUserId())) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_FORBIDDEN, "无权限加入频道");
            }

            // 3. 验证频道是否存在
            ChatChannelPO channel = iChatChannelMapper.selectById(request.getChatChannelId());
            if (channel == null) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_NOT_FOUND, "语音频道不存在");
            }

            // 4. 检查频道容量
            long memberCount = iChatChannelMemberMapper.selectCount(
                    new QueryWrapper<ChatChannelMemberPO>()
                            .eq("chat_channel_id", request.getChatChannelId())
            );
            if (memberCount >= channel.getCapacity()) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_FORBIDDEN, "频道人数已满");
            }

            // 5. 添加频道成员
            ChatChannelMemberPO member = new ChatChannelMemberPO();
            member.setChatChannelId(request.getChatChannelId());
            member.setUserId(UserContext.getUserId());
            if (iChatChannelMemberMapper.insert(member) != 1) {
                throw new DataAccessException("加入频道失败") {};
            }

            // 6. 获取更新后的频道成员
            List<ChatChannelMemberPO> channelMembers = iChatChannelMemberMapper.selectList(
                    new QueryWrapper<ChatChannelMemberPO>()
                            .eq("chat_channel_id", request.getChatChannelId())
            );

            // 7. 获取成员用户信息
            Map<Long, UserViewDTO> userViewMap = getUserViewMap(
                    channelMembers.stream()
                            .map(ChatChannelMemberPO::getUserId)
                            .collect(Collectors.toList())
            );

            // 8. 构建完整响应
            ChatChannelDTO channelDTO = CommunityUtils.buildChatChannelDTO(channel);
            List<UserViewDTO> members = channelMembers.stream()
                    .map(m -> userViewMap.getOrDefault(m.getUserId(),
                            new UserViewDTO(m.getUserId(), "未知用户", "", null, null, null)))
                    .collect(Collectors.toList());

            response.setData(new ChatChannelAndMemberMultiResponse(channelDTO, members));
            response.setCode(HttpStatus.SC_CREATED);
            response.setMessage("成功加入语音频道");

            // TODO: WebRTC 频道加入
        }
        catch (DuplicateKeyException e) {
            return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_CONFLICT, "用户已在频道中");
        }
        catch (DataAccessException e) {
            CommunityUtils.handleDatabaseError(response, "加入频道失败", e);
        }
        catch (Exception e) {
            CommunityUtils.handleGenericError(response, "加入频道时发生未知错误", e);
        }
        return response;
    }

    @Override
    public BaseResponse<Void> leaveChatChannel(ChatChannelIdRequest chatChannelIdRequest)
    {
        BaseResponse<Void> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            if(UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID未登录");
                return response;
            }

            ChatChannelPO channel = iChatChannelMapper.selectOne(
                    new QueryWrapper<ChatChannelPO>()
                            .eq("chat_channel_id", chatChannelIdRequest.getChatChannelId())
                            .eq("community_id", chatChannelIdRequest.getCommunityId())
            );
            if(channel == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("频道不存在");
                return response;
            }

            if(!iCommunityMemberMapper.exists(
                    new QueryWrapper<CommunityMemberPO>()
                            .eq("chat_channel_id", chatChannelIdRequest.getChatChannelId())
                            .eq("user_id", UserContext.getUserId())
            ))
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不在频道中");
                return response;
            }

            if(iChatChannelMemberMapper.delete(
                    new QueryWrapper<ChatChannelMemberPO>()
                            .eq("chat_channel_id", chatChannelIdRequest.getChatChannelId())
                            .eq("user_id", UserContext.getUserId())
            ) != 1)
            {
                throw new DataAccessException("离开频道失败") {};
            }

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("成功离开语音频道");
        }
        catch (DataAccessException e)
        {
            CommunityUtils.handleDatabaseError(response, "离开频道时发生未知错误", e);
        }
        catch (Exception e)
        {
            CommunityUtils.handleGenericError(response, "离开频道时发生未知错误", e);
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Void> dismissChatChannel(Long chatChannelId, Long communityId)
    {
        BaseResponse<Void> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 验证用户权限
            CommunityMemberPO communityMember = iCommunityMemberMapper.selectOne(
                    new QueryWrapper<CommunityMemberPO>()
                            .eq("community_id", communityId)
                            .eq("user_id", UserContext.getUserId())
            );

            if (communityMember == null ||
                    (communityMember.getRole() != MemberRole.OWNER &&
                            communityMember.getRole() != MemberRole.ADMIN)) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_FORBIDDEN, "无权限解散频道");
            }

            // 2. 删除频道及成员关系
            iChatChannelMemberMapper.delete(
                    new QueryWrapper<ChatChannelMemberPO>()
                            .eq("chat_channel_id", chatChannelId)
            );

            if (iChatChannelMapper.deleteById(chatChannelId) != 1) {
                throw new DataAccessException("频道删除失败") {};
            }

            // TODO: 调用WebRTC服务关闭频道

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("频道解散成功");

        } catch (DataAccessException e) {
            CommunityUtils.handleDatabaseError(response, "解散频道失败", e);
        } catch (Exception e) {
            CommunityUtils.handleGenericError(response, "解散频道时发生未知错误", e);
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<ChatChannelResponse> updateChatChannel(ChatChannelRequest request)
    {
        BaseResponse<ChatChannelResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 权限验证
            CommunityMemberPO communityMember = iCommunityMemberMapper.selectOne(
                    new QueryWrapper<CommunityMemberPO>()
                            .eq("community_id", request.getCommunityId())
                            .eq("user_id", UserContext.getUserId())
            );

            if (communityMember == null ||
                    (communityMember.getRole() != MemberRole.OWNER &&
                            communityMember.getRole() != MemberRole.ADMIN)) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_FORBIDDEN, "无权限修改频道");
            }

            // 2. 获取并更新频道信息
            ChatChannelPO channel = iChatChannelMapper.selectById(request.getChatChannelId());
            if (channel == null) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_NOT_FOUND, "频道不存在");
            }

            if (request.getName() != null) channel.setName(request.getName());
            if (request.getCapacity() != null) channel.setCapacity(request.getCapacity());

            if (iChatChannelMapper.updateById(channel) != 1) {
                throw new DataAccessException("频道更新失败") {};
            }

            // 3. 构建响应
            ChatChannelDTO dto = CommunityUtils.buildChatChannelDTO(channel);
            response.setData(new ChatChannelResponse(dto));
            response.setCode(HttpStatus.SC_OK);
            response.setMessage("频道信息更新成功");

        } catch (DataAccessException e) {
            CommunityUtils.handleDatabaseError(response, "更新频道失败", e);
        } catch (Exception e) {
            CommunityUtils.handleGenericError(response, "更新频道时发生未知错误", e);
        }
        return response;
    }

    @Override
    public BaseResponse<ChatChannelMultiAndMemberMultiResponse> getChatChannelsAndMembersByCommunityId(Long communityId)
    {
        BaseResponse<ChatChannelMultiAndMemberMultiResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 权限验证
            if (!isCommunityMember(communityId, UserContext.getUserId())) {
                return CommunityUtils.buildErrorResponse(response, HttpStatus.SC_FORBIDDEN, "无权限访问");
            }

            // 2. 获取频道及成员信息
            List<ChatChannelAndMemberMultiDTO> channels = buildChatChannelsWithMembers(communityId);

            // 3. 构建响应
            response.setData(new ChatChannelMultiAndMemberMultiResponse(channels));
            response.setCode(HttpStatus.SC_OK);
            response.setMessage("成功获取语音频道信息");

        } catch (DataAccessException e) {
            CommunityUtils.handleDatabaseError(response, "获取频道信息失败", e);
        } catch (Exception e) {
            CommunityUtils.handleGenericError(response, "获取频道信息时发生未知错误", e);
        }
        return response;
    }

    private Map<Long, UserViewDTO> getUserViewMap(List<Long> userIds)
    {
        if (userIds.isEmpty()) return Collections.emptyMap();

        try {
            UserIdMultiRequest userIdMultiRequest = new UserIdMultiRequest();
            userIdMultiRequest.setUserIds(userIds);
            ResponseEntity<BaseResponse<UserViewMultiResponse>> response =
                    userFeignClient.getUserViewMultiByUserIdMulti(userIdMultiRequest);

            if (response.getStatusCodeValue() == HttpStatus.SC_OK && response.getBody() != null) {
                return response.getBody().getData().getUserViews().stream()
                        .collect(Collectors.toMap(UserViewDTO::getUserId, Function.identity()));
            }
            return Collections.emptyMap();
        } catch (FeignException e) {
            log.error("用户服务调用异常", e);
            return Collections.emptyMap(); // 降级处理
        }
    }

    private List<ChatChannelAndMemberMultiDTO> buildChatChannelsWithMembers(Long communityId)
    {
        // 1. 获取所有语音频道
        List<ChatChannelPO> channels = iChatChannelMapper.selectList(
                new QueryWrapper<ChatChannelPO>().eq("community_id", communityId)
        );

        // 2. 批量获取频道成员信息
        Map<Long, List<Long>> channelMemberMap = channels.stream()
                .collect(Collectors.toMap(
                        ChatChannelPO::getChatChannelId,
                        channel -> iChatChannelMemberMapper.selectList(
                                        new QueryWrapper<ChatChannelMemberPO>()
                                                .eq("chat_channel_id", channel.getChatChannelId())
                                ).stream()
                                .map(ChatChannelMemberPO::getUserId)
                                .collect(Collectors.toList())
                ));

        // 3. 合并所有用户ID（去重）
        Set<Long> allUserIds = channelMemberMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        // 4. 批量获取用户视图
        Map<Long, UserViewDTO> userViewMap = getUserViewMap(new ArrayList<>(allUserIds));

        // 5. 构建DTO
        return channels.stream().map(channel -> {
            List<UserViewDTO> members = channelMemberMap.getOrDefault(channel.getChatChannelId(), Collections.emptyList())
                    .stream()
                    .map(userId -> userViewMap.getOrDefault(userId, new UserViewDTO(userId, "已注销用户", "", null, null, null)))
                    .collect(Collectors.toList());

            return new ChatChannelAndMemberMultiDTO(
                    channel.getChatChannelId(),
                    channel.getCommunityId(),
                    channel.getName(),
                    channel.getCapacity(),
                    members
            );
        }).collect(Collectors.toList());
    }

    private boolean isCommunityMember(Long communityId, Long userId)
    {
        if (userId == null) return false;
        return iCommunityMemberMapper.exists(
                new QueryWrapper<CommunityMemberPO>()
                        .eq("community_id", communityId)
                        .eq("user_id", userId)
        );
    }
}
