package com.mvtalker.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mvtalker.user.entity.po.UserStatusPO;
import com.mvtalker.user.mapper.*;
import com.mvtalker.user.tool.UserUtils;
import com.mvtalker.utilities.entity.community.dto.CommunityMemberDTO;
import com.mvtalker.utilities.entity.user.dto.*;
import com.mvtalker.utilities.entity.user.request.*;
import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.user.response.*;
import com.mvtalker.utilities.entity.user.enums.*;
import com.mvtalker.user.entity.po.*;
import com.mvtalker.user.service.interfaces.IUserService;
import com.mvtalker.user.tool.EncryptionUtils;
import com.mvtalker.utilities.common.UserContext;
import com.mvtalker.utilities.feign.CommunityFeignClient;
import com.mvtalker.utilities.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService
{
    private final IUserInfoMapper iUserInfoMapper;
    private final IUserDeviceMapper iUserDeviceMapper;
    private final IUserStatusMapper iUserStatusMapper;
    private final IUserLocalVolumeMapper iUserLocalVolumeMapper;
    private final IUserGlobalVolumeMapper iUserGlobalVolumeMapper;
    private final EncryptionUtils encryptionUtils;
    //private final IpGeoParserUtils ipGeoParserUtils;
    private final JwtUtil jwtUtil;
    private final CommunityFeignClient communityFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<LoginResponse> login(UserAuthInfoRequest userAuthInfoRequest)
    {
        BaseResponse<LoginResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 查询用户
            UserInfoPO userInfoPO = iUserInfoMapper.selectByMobile(userAuthInfoRequest.getMobile());
            if (userInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 2. 检查用户状态
            UserStatusPO userStatusPO = iUserStatusMapper.selectById(userInfoPO.getUserId());
            if (userStatusPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }
            if(userStatusPO.getOnlineStatus() != OnlineStatus.OFFLINE)
            {
                response.setCode(HttpStatus.SC_FORBIDDEN);
                response.setMessage("用户已登录");
                return response;
            }

            // 3 在用户上下文中设置用户ID，因为网关层放行了该请求，因此用户上下文为空
            UserContext.setUserId(userInfoPO.getUserId());

            // 4. 验证密码
            if (!encryptionUtils.matches(userAuthInfoRequest.getPassword(), userInfoPO.getPasswordEncrypted()))
            {
                response.setCode(HttpStatus.SC_UNAUTHORIZED);
                response.setMessage("密码错误");
                return response;
            }

            // 5. 生成JWT
            String token;
            try
            {
                token = jwtUtil.generateJwt(UserContext.getUserId());
            }
            catch (Exception e)
            {
                log.error("JWT生成失败: {}", e.getMessage());
                response.setCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
                response.setMessage("认证服务暂不可用");
                return response;
            }

            // 6. 更新用户状态
            UserStatusPO userStatus = new UserStatusPO();
            userStatus.setUserId(UserContext.getUserId());
            userStatus.setOnlineStatus(OnlineStatus.ONLINE);
            userStatus.setLastOnline(LocalDateTime.now());
            iUserStatusMapper.updateById(userStatus);

            // 7. 构建返回数据
            UserStatusDTO userStatusDto = UserUtils.buildUserStatusDTO(iUserStatusMapper.selectById(UserContext.getUserId()));
            UserInfoDTO userInfoDto = UserUtils.buildUserInfoDTO(userInfoPO);
            UserGlobalVolumeDTO userGlobalVolumeDto = UserUtils.buildUserGlobalVolumeDTO(iUserGlobalVolumeMapper.selectById(UserContext.getUserId()));

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("登录成功");
            response.setData(new LoginResponse(token, userInfoDto, userStatusDto, userGlobalVolumeDto));

        }
        catch (DataAccessException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据存储失败");
        }
        catch (Exception e)
        {
            log.error("登录过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务注解保证原子性，用户和设备插入操作需要在一个事务中
    public BaseResponse<LoginResponse> register(RegisterRequest registerRequest)
    {
        BaseResponse<LoginResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 手机号 E.164 格式校验（其实也已经在DTO里校验过了）
            if (!UserUtils.isValidMobile(registerRequest.getMobile()))
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("手机号格式不正确");
                return response;
            }

            // 2. 检查手机号唯一性
            if (iUserInfoMapper.selectByMobile(registerRequest.getMobile()) != null)
            {
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("手机号已被注册");
                response.setTimestamp(LocalDateTime.now());
                return response;
            }

            // 3. 密码强度校验
            if (!UserUtils.isPasswordStrong(registerRequest.getPassword())) {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("密码必须包含字母、数字且长度8位以上");
                return response;
            }

            // 4. 拿到密码明文，加密
            String encodedPwd = encryptionUtils.encode(registerRequest.getPassword());

            // TODO 加密用户手机号

            // 5. 构建用户信息实体、用户状态实体、用户全局音量配置实体
            UserInfoPO userInfoPO = new UserInfoPO();
            userInfoPO.setMobileEncrypted(registerRequest.getMobile());
            userInfoPO.setPasswordEncrypted(encodedPwd);
            userInfoPO.setNickname(registerRequest.getNickname());
            userInfoPO.setAvatarUrl(registerRequest.getAvatarUrl());
            if (iUserInfoMapper.insert(userInfoPO) != 1)
            {
                throw new DataAccessException("用户信息数据插入失败") {};
            }

            UserStatusPO userStatusPO = new UserStatusPO();
            userStatusPO.setUserId(userInfoPO.getUserId());
            userStatusPO.setOnlineStatus(OnlineStatus.ONLINE);
            userStatusPO.setLastOnline(LocalDateTime.now());
            userStatusPO.setAccountStatus(AccountStatus.NORMAL);
            userStatusPO.setVisibility(UserVisibility.PUBLIC);
            if (iUserStatusMapper.insert(userStatusPO) != 1)
            {
                throw new DataAccessException("用户状态数据插入失败") {};
            }

            UserGlobalVolumePO userGlobalVolumePO = new UserGlobalVolumePO();
            userGlobalVolumePO.setUserId(userInfoPO.getUserId());
            userGlobalVolumePO.setOutputVolume(100);
            userGlobalVolumePO.setOutputActive(true);
            userGlobalVolumePO.setInputVolume(100);
            userGlobalVolumePO.setInputActive(true);
            if (iUserGlobalVolumeMapper.insert(userGlobalVolumePO) != 1)
            {
                throw new DataAccessException("用户全局音量配置数据插入失败") {};
            }

            // 6. 设置用户上下文
            UserContext.setUserId(userInfoPO.getUserId());

            // 7. JWT
            String token;
            try
            {
                token = jwtUtil.generateJwt(UserContext.getUserId());
            }
            catch (Exception e)
            {
                log.error("JWT生成失败: {}", e.getMessage());
                response.setCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
                response.setMessage("系统服务暂时不可用");
                return response;
            }

            // 8. 构建返回值
            UserStatusDTO userStatusDto = UserUtils.buildUserStatusDTO(iUserStatusMapper.selectById(UserContext.getUserId()));
            UserInfoDTO userInfoDto = UserUtils.buildUserInfoDTO(userInfoPO);
            UserGlobalVolumeDTO userGlobalVolumeDto = UserUtils.buildUserGlobalVolumeDTO(iUserGlobalVolumeMapper.selectById(UserContext.getUserId()));

            response.setCode(HttpStatus.SC_CREATED);  // 201 Created更符合REST规范
            response.setData(new LoginResponse(token, userInfoDto, userStatusDto, userGlobalVolumeDto));
            response.setMessage("注册成功");
        }
        catch (DuplicateKeyException e)
        {
            log.warn("重复注册尝试 手机号: {}", registerRequest.getMobile());
            response.setCode(HttpStatus.SC_CONFLICT);
            response.setMessage("手机号已被注册");
            return response;
        }
        catch (DataAccessException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据存储失败");
        }
        catch (Exception e)
        {
            log.error("注册过程未知异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Void> offline()
    {
        BaseResponse<Void> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 获取当前用户ID
            Long userId = UserContext.getUserId();
            if (userId == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户未登录");
                return response;
            }

            // 2. 验证用户存在性
            UserInfoPO userInfo = iUserInfoMapper.selectById(userId);
            if (userInfo == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 3. 更新用户状态
            UserStatusPO status = new UserStatusPO();
            status.setUserId(userId);
            status.setOnlineStatus(OnlineStatus.OFFLINE);
            status.setLastOnline(LocalDateTime.now());

            if (iUserStatusMapper.updateById(status) != 1)
            {
                throw new DataAccessException("状态更新失败") {};
            }

            // 5. 构建响应
            response.setCode(HttpStatus.SC_OK);
            response.setMessage("退出成功");

        }
        catch (DataAccessException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("状态更新失败");
        }
        catch (Exception e)
        {
            log.error("退出过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<UserInfoResponse> updateUserBaseInfo(UserBaseInfoRequest userBaseInfoRequest)
    {
        BaseResponse<UserInfoResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            // 1. 查询现有用户数据
            UserInfoPO userInfoPO = iUserInfoMapper.selectById(UserContext.getUserId());
            if (userInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 2. 构建用户实体类
            userInfoPO.setNickname(userBaseInfoRequest.getNickname());
            userInfoPO.setAvatarUrl(userBaseInfoRequest.getAvatarUrl());

            // 3. 持久化用户实体类
            try
            {
                if (iUserInfoMapper.updateById(userInfoPO) != 1)
                {
                    throw new DataAccessException("用户基本信息更新失败") {};
                }
            }
            catch (DuplicateKeyException e)
            {
                log.error("唯一性冲突: {}", e.getMessage());
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("数据冲突，请检查输入内容");
                return response;
            }

            // 4. 获取更新后的完整数据
            UserInfoPO userInfo = iUserInfoMapper.selectById(UserContext.getUserId());
            // TODO: 解密手机号
            UserInfoDTO userInfoDto = UserUtils.buildUserInfoDTO(userInfo);

            // 5. 构建返回结果
            UserInfoResponse userInfoResponse = new UserInfoResponse(userInfoDto);

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("用户基本信息更新成功");
            response.setData(userInfoResponse);

        }
        catch (DataAccessException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据更新失败");
        }
        catch (Exception e)
        {
            log.error("更新过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
        }

        return response;
    }

    @Override
    public BaseResponse<UserInfoResponse> updateUserAuthInfo(UserAuthInfoRequest userAuthInfoRequest)
    {
        BaseResponse<UserInfoResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            UserInfoPO userInfoPO = iUserInfoMapper.selectById(UserContext.getUserId());
            if (userInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // TODO: 验证用户权限

            // TODO: 密码强度校验
            if(!UserUtils.isPasswordStrong(userAuthInfoRequest.getPassword()))
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("密码强度不够");
                return response;
            }

            userInfoPO.setMobileEncrypted(userAuthInfoRequest.getMobile());
            userInfoPO.setPasswordEncrypted(encryptionUtils.encode(userAuthInfoRequest.getPassword()));

            try
            {
                if (iUserInfoMapper.updateById(userInfoPO) != 1)
                {
                    throw new DataAccessException("用户鉴权信息更新失败") {};
                }
            }
            catch (DuplicateKeyException e)
            {
                log.error("唯一性冲突: {}", e.getMessage());
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("数据冲突，请检查输入内容");
                return response;
            }

            UserInfoPO userInfo = iUserInfoMapper.selectById(UserContext.getUserId());
            // TODO: 解密手机号
            UserInfoDTO userInfoDto = UserUtils.buildUserInfoDTO(userInfo);

            UserInfoResponse userInfoResponse = new UserInfoResponse(userInfoDto);

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("用户鉴权信息更新成功");
            response.setData(userInfoResponse);

        }
        catch (DataAccessException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据更新失败");
        }
        catch (Exception e)
        {
            log.error("更新过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
        }

        return response;
    }

    @Override
    public BaseResponse<UserStatusResponse> updateUserStatus(UserStatusRequest userStatusRequest)
    {
        BaseResponse<UserStatusResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            // 1. 查询现有用户数据
            UserStatusPO userStatusPO = iUserStatusMapper.selectById(UserContext.getUserId());
            if (userStatusPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 2. 构建实体类
            userStatusPO.setOnlineStatus(userStatusRequest.getOnlineStatus());
            userStatusPO.setVisibility(userStatusRequest.getVisibility());

            // 3. 持久化实体类
            try
            {
                if (iUserStatusMapper.updateById(userStatusPO) != 1)
                {
                    throw new DataAccessException("用户状态更新失败") {};
                }
            }
            catch (DuplicateKeyException e)
            {
                log.error("唯一性冲突: {}", e.getMessage());
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("数据冲突，请检查输入内容");
                return response;
            }

            // 4. 获取更新后的完整数据
            UserStatusPO userStatus = iUserStatusMapper.selectById(UserContext.getUserId());
            UserStatusDTO userStatusDto = UserUtils.buildUserStatusDTO(userStatus);

            // 5. 构建返回结果
            UserStatusResponse userStatusResponse = new UserStatusResponse(userStatusDto);

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("用户状态更新成功");
            response.setData(userStatusResponse);

        }
        catch (DataAccessException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据更新失败");
        }
        catch (Exception e)
        {
            log.error("更新过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
        }

        return response;
    }

    @Override
    public BaseResponse<UserGlobalVolumeResponse> updateUserGlobalVolume(UserGlobalVolumeRequest userGlobalVolumeRequest)
    {
        BaseResponse<UserGlobalVolumeResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            // 1. 查询现有用户数据
            UserGlobalVolumePO userGlobalVolumePO = iUserGlobalVolumeMapper.selectById(UserContext.getUserId());
            if (userGlobalVolumePO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 2. 构建实体类
            userGlobalVolumePO.setOutputVolume(userGlobalVolumeRequest.getOutputVolume());
            userGlobalVolumePO.setOutputActive(userGlobalVolumeRequest.getOutputActive());
            userGlobalVolumePO.setInputVolume(userGlobalVolumeRequest.getInputVolume());
            userGlobalVolumePO.setInputActive(userGlobalVolumeRequest.getInputActive());

            // 3. 持久化实体类
            try
            {
                if (iUserGlobalVolumeMapper.updateById(userGlobalVolumePO) != 1)
                {
                    throw new DataAccessException("用户全局音量配置更新失败") {};
                }
            }
            catch (DuplicateKeyException e)
            {
                log.error("唯一性冲突: {}", e.getMessage());
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("数据冲突，请检查输入内容");
                return response;
            }

            // 4. 获取更新后的完整数据
            UserGlobalVolumePO userGlobalVolume = iUserGlobalVolumeMapper.selectById(UserContext.getUserId());
            UserGlobalVolumeDTO userGlobalVolumeDto = UserUtils.buildUserGlobalVolumeDTO(userGlobalVolume);

            // 5. 构建返回结果
            UserGlobalVolumeResponse userGlobalVolumeResponse = new UserGlobalVolumeResponse(userGlobalVolumeDto);

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("用户全局音量配置更新成功");
            response.setData(userGlobalVolumeResponse);

        }
        catch (DataAccessException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据更新失败");
        }
        catch (Exception e)
        {
            log.error("更新过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
        }

        return response;
    }

    @Override
    public BaseResponse<UserLocalVolumeResponse> updateUserLocalVolume(UserLocalVolumeRequest userLocalVolumeRequest)
    {
        BaseResponse<UserLocalVolumeResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            if (Objects.equals(userLocalVolumeRequest.getTargetId(), UserContext.getUserId()))
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("不能对自己调节局部音量");
                return response;
            }

            // TODO: 检查用户与被调节用户的关系是否合法（如：是否加入了同一个频道）

            // 1. 查询现有用户数据
            boolean isExist = iUserLocalVolumeMapper.exists(
                    new QueryWrapper<UserLocalVolumePO>()
                            .eq("source_id", UserContext.getUserId())
                            .eq("target_id", userLocalVolumeRequest.getTargetId())
            );

            // 2. 持久化实体类（之前没有就新增一条记录，有就更新该记录）
            try
            {
                if (!isExist)
                {
                    UserLocalVolumePO userLocalVolumePO = new UserLocalVolumePO();
                    userLocalVolumePO.setInputVolume(userLocalVolumeRequest.getInputVolume());
                    userLocalVolumePO.setInputActive(userLocalVolumeRequest.getInputActive());
                    userLocalVolumePO.setSourceId(UserContext.getUserId());
                    userLocalVolumePO.setTargetId(userLocalVolumeRequest.getTargetId());
                    if (iUserLocalVolumeMapper.insert(userLocalVolumePO) != 1)
                    {
                        throw new DataAccessException("用户局部音量配置插入失败") {};
                    }
                }
                else
                {
                    UserLocalVolumePO userLocalVolumePO = iUserLocalVolumeMapper.selectBySourceAndTarget(UserContext.getUserId(), userLocalVolumeRequest.getTargetId());
                    userLocalVolumePO.setAgentId(userLocalVolumePO.getAgentId());
                    userLocalVolumePO.setInputVolume(userLocalVolumeRequest.getInputVolume());
                    userLocalVolumePO.setInputActive(userLocalVolumeRequest.getInputActive());
                    userLocalVolumePO.setSourceId(UserContext.getUserId());
                    userLocalVolumePO.setTargetId(userLocalVolumeRequest.getTargetId());
                    if (iUserLocalVolumeMapper.updateById(userLocalVolumePO) != 1)
                    {
                        throw new DataAccessException("用户局部音量配置更新失败") {};
                    }
                }

            }
            catch (DuplicateKeyException e)
            {
                log.error("唯一性冲突: {}", e.getMessage());
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("数据冲突，请检查输入内容");
                return response;
            }

            // 3. 获取更新后的完整数据
            UserLocalVolumePO userLocalVolume = iUserLocalVolumeMapper.selectBySourceAndTarget(UserContext.getUserId(), userLocalVolumeRequest.getTargetId());
            UserLocalVolumeDTO userLocalVolumeDto = UserUtils.buildUserLocalVolumeDTO(userLocalVolume);

            // 4. 构建返回结果
            UserLocalVolumeResponse userLocalVolumeResponse = new UserLocalVolumeResponse(userLocalVolumeDto);

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("用户局部音量配置更新成功");
            response.setData(userLocalVolumeResponse);

        }
        catch (DataAccessException e)
        {
            log.error("数据库操作异常: {}", e.getMessage());
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("数据更新失败");
        }
        catch (Exception e)
        {
            log.error("更新过程异常: {}", e.getMessage(), e);
            response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("系统内部错误");
        }

        return response;
    }

    @Override
    public BaseResponse<UserInfoResponse> getUserInfoByUserId()
    {
        BaseResponse<UserInfoResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 校验用户上下文
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            // 2. 查询并校验记录
            UserInfoPO userInfoPO = iUserInfoMapper.selectById(UserContext.getUserId());
            if (userInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 3. 查询并构建返回结果
            UserInfoResponse userInfoResponse = new UserInfoResponse(UserUtils.buildUserInfoDTO(userInfoPO));

            // TODO: 解密用户手机号

            response.setCode(HttpStatus.SC_OK);
            response.setData(userInfoResponse);
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

    @Override
    public BaseResponse<UserStatusResponse> getUserStatusByUserId()
    {
        BaseResponse<UserStatusResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 校验用户上下文
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            // 2. 查询并校验记录
            UserStatusPO userStatusPO = iUserStatusMapper.selectById(UserContext.getUserId());
            if (userStatusPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 3. 查询并构建返回结果
            UserStatusResponse userStatusResponse = new UserStatusResponse(UserUtils.buildUserStatusDTO(userStatusPO));

            response.setCode(HttpStatus.SC_OK);
            response.setData(userStatusResponse);
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

    @Override
    public BaseResponse<UserViewMultiResponse> getUserViewMultiByUserIdMulti(UserIdMultiRequest userIdMultiRequest)
    {
        BaseResponse<UserViewMultiResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            List<Long> userIds = userIdMultiRequest.getUserIds();

            // 1. 校验用户上下文
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            // TODO: 2. 校验发起方用户与被查询用户关系是否合法

            // 3. 查询并构建返回结果
            List<UserInfoPO> UserInfoPOs = iUserInfoMapper.selectBatchIds(userIds);
            // TODO: 空值校验
            List<UserStatusPO> UserStatusPOs = iUserStatusMapper.selectBatchIds(userIds);
            // TODO: 空值校验
            List<UserViewDTO> userViewDTOs = UserUtils.buildUserViewDTOList(UserInfoPOs, UserStatusPOs);

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

    @Override
    public BaseResponse<UserViewResponse> getUserViewByUserId(Long userId)
    {
        BaseResponse<UserViewResponse> response = new BaseResponse<>();
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

            // TODO: 2. 校验发起方用户与被查询用户关系是否合法

            // 3. 查询并构建返回结果
            UserInfoPO userInfoPO = iUserInfoMapper.selectById(userId);
            if (userInfoPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户信息数据不存在");
                log.error("用户信息数据不存在");
                return response;
            }
            UserStatusPO userStatusPO = iUserStatusMapper.selectById(userId);
            if (userStatusPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户状态数据不存在");
                log.error("用户状态数据不存在");
                return response;
            }
            UserViewDTO userViewDTO = UserUtils.buildUserViewDTO(userInfoPO, userStatusPO);

            UserViewResponse userViewResponse = new UserViewResponse(userViewDTO);

            response.setCode(HttpStatus.SC_OK);
            response.setData(userViewResponse);
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

    @Override
    public BaseResponse<UserGlobalVolumeResponse> getUserGlobalVolumeByUserId()
    {
        BaseResponse<UserGlobalVolumeResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 校验用户上下文
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            // 2. 查询并校验记录
            UserGlobalVolumePO userGlobalVolumePO = iUserGlobalVolumeMapper.selectById(UserContext.getUserId());
            if (userGlobalVolumePO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 3. 构建返回结果
            UserGlobalVolumeResponse userGlobalVolumeResponse = new UserGlobalVolumeResponse(UserUtils.buildUserGlobalVolumeDTO(userGlobalVolumePO));

            response.setCode(HttpStatus.SC_OK);
            response.setData(userGlobalVolumeResponse);
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

    @Override
    public BaseResponse<UserLocalVolumeMultiResponse> getUserLocalVolumeByUserIdAndCommunityId(Long communityId)
    {
        BaseResponse<UserLocalVolumeMultiResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 校验用户上下文
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            // TODO: 2. 校验用户是否在社区中
            // Feign远程调用，通过社区ID获取社区成员列表
            List<CommunityMemberDTO> communityMembers = null;

            // TODO: 3. 根据用户ID以及社区成员ID查询记录，不用校验记录是否为空
            List<UserLocalVolumePO> userLocalVolumePOs = null;
            List<UserLocalVolumeDTO> userLocalVolumeDTOs = userLocalVolumePOs.stream().map(UserUtils::buildUserLocalVolumeDTO).collect(Collectors.toList());

            // 4. 构建返回结果
            UserLocalVolumeMultiResponse userLocalVolumeMultiResponse = new UserLocalVolumeMultiResponse(userLocalVolumeDTOs);

            response.setCode(HttpStatus.SC_OK);
            response.setData(userLocalVolumeMultiResponse);
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

    // TODO: 记得问一下AI哪些业务是需要事务注解的
}
