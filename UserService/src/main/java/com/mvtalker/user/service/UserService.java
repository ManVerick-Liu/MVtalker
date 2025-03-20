package com.mvtalker.user.service;

import com.mvtalker.user.entity.dto.DeviceBaseDTO;
import com.mvtalker.user.entity.dto.DeviceLoginDTO;
import com.mvtalker.user.entity.dto.UserBaseDTO;
import com.mvtalker.user.entity.dto.request.LoginRequest;
import com.mvtalker.user.entity.dto.request.RegisterRequest;
import com.mvtalker.user.entity.dto.request.UpdateRequest;
import com.mvtalker.utilities.entity.dto.response.BaseResponse;
import com.mvtalker.user.entity.dto.response.LoginResponse;
import com.mvtalker.user.entity.dto.response.SearchResponse;
import com.mvtalker.user.entity.enums.OnlineStatus;
import com.mvtalker.user.entity.enums.UserStatus;
import com.mvtalker.user.entity.enums.Visibility;
import com.mvtalker.user.entity.po.DevicePO;
import com.mvtalker.user.entity.po.UserPO;
import com.mvtalker.user.mapper.IDeviceMapper;
import com.mvtalker.user.mapper.IUserMapper;
import com.mvtalker.user.service.interfaces.IUserService;
import com.mvtalker.user.tool.EncryptionUtils;
import com.mvtalker.user.tool.IpGeoParserUtils;
import com.mvtalker.utilities.common.UserContext;
import com.mvtalker.utilities.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService
{
    private final IUserMapper iUserMapper;
    private final IDeviceMapper iDeviceMapper;
    private final EncryptionUtils encryptionUtils;
    private final IpGeoParserUtils ipGeoParserUtils;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<LoginResponse> login(LoginRequest loginRequest, HttpServletRequest httpRequest)
    {
        BaseResponse<LoginResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try {
            // 1. 参数校验
            if (loginRequest.getMobile() == null || loginRequest.getMobile().isEmpty())
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("手机号不能为空");
                return response;
            }

            // 2. 查询用户
            UserPO userPO = iUserMapper.selectByMobile(loginRequest.getMobile());
            if (userPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 2.1 在用户上下文中设置用户ID，因为网关层放行了该请求，因此用户上下文为空
            UserContext.setUserId(userPO.getId());

            // 3. 验证密码
            if (!encryptionUtils.matches(loginRequest.getPassword(), userPO.getPasswordEncrypted()))
            {
                response.setCode(HttpStatus.SC_UNAUTHORIZED);
                response.setMessage("密码错误");
                return response;
            }

            // 4. 生成并匹配设备指纹（新的设备指纹意味着用户使用新的设备登录），获取设备实体
            String currentDeviceId = generateDeviceId(userPO.getId(), loginRequest.getPlatform().name());
            DevicePO devicePO = iDeviceMapper.selectByDeviceId(currentDeviceId);

            // 4.1 设备不存在处理
            if (devicePO == null) {
                response.setCode(HttpStatus.SC_FORBIDDEN);
                response.setMessage("未授权的设备登录");
                log.warn("非法设备登录尝试 用户ID:{} 设备指纹:{}",
                        userPO.getId(), currentDeviceId);
                return response;
            }

            // 4.2 设备状态检查
            if (devicePO.getOnlineStatus() == OnlineStatus.ONLINE)
            {
                // 根据业务需求选择处理方式：
                // 方案1：强制踢下线（更新状态）
//                devicePO.setOnlineStatus(OnlineStatus.OFFLINE);
//                iDeviceMapper.updateById(devicePO);
//                log.info("强制下线重复登录设备 用户ID:{} 设备ID:{}",
//                        userPO.getId(), devicePO.getDeviceId());

                // 方案2：返回错误提示
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("该设备已在线，请勿重复登录");
                return response;
            }

            // 5. 构建并持久化设备信息（含IP地理位置）
            devicePO.setLastOnline(LocalDateTime.now());
            devicePO.setOnlineStatus(OnlineStatus.ONLINE); // 用户登录后，在线状态就设为在线
            devicePO.setClientVersion(loginRequest.getClientVersion());
            devicePO.setIpGeo(ipGeoParserUtils.parse(ipGeoParserUtils.getClientIp(httpRequest)));

            try
            {
                iDeviceMapper.updateById(devicePO);
            } catch (Exception e)
            {
                log.error("设备状态更新失败 用户ID:{} 设备ID:{} 错误:{}",
                        userPO.getId(), devicePO.getDeviceId(), e.getMessage());
                response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                response.setMessage("设备状态更新失败");
                return response;
            }

            // 5. 生成JWT
            String token;
            try
            {
                //token = gatewayFeignClient.generateJwt(userPO.getId());
                token = jwtUtil.generateJwt(UserContext.getUserId());
            }
            catch (Exception e)
            {
                log.error("JWT生成失败: {}", e.getMessage());
                response.setCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
                response.setMessage("认证服务暂不可用");
                return response;
            }

            // 6. 构建返回数据
            UserBaseDTO userInfo = buildUserBaseDTO(userPO);
            DeviceLoginDTO deviceInfo = buildDeviceLoginDTO(devicePO);

            LoginResponse loginResponse = new LoginResponse(token, userInfo, deviceInfo);

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("登录成功");
            response.setData(loginResponse);

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
    public BaseResponse<LoginResponse> register(RegisterRequest registerRequest, HttpServletRequest httpRequest)
    {
        BaseResponse<LoginResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 手机号 E.164 格式校验
            if (!isValidMobile(registerRequest.getMobile()))
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("手机号格式不正确");
                return response;
            }

            // 手机号存在性检查
            if (iUserMapper.selectByMobile(registerRequest.getMobile()) != null)
            {
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("手机号已被注册");
                response.setTimestamp(LocalDateTime.now());
                return response;
            }

            // 密码强度校验
            if (!isPasswordStrong(registerRequest.getPassword())) {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("密码必须包含字母、数字且长度8位以上");
                return response;
            }

            // 拿到密码明文，加密
            String encodedPwd = encryptionUtils.encode(registerRequest.getPassword());

            // 构建用户实体
            UserPO userPO = new UserPO();
            // 不用写雪花算法生成id的逻辑，在UserPO中使用Mybatis-Plus配置好了，会自动处理
            // 当前暂时不对手机号进行加密
            userPO.setMobileEncrypted(registerRequest.getMobile());
            userPO.setPasswordEncrypted(encodedPwd);
            userPO.setNickname(registerRequest.getNickname());
            userPO.setAvatarUrl(registerRequest.getAvatarUrl());
            userPO.setStatus(UserStatus.NORMAL);
            userPO.setVisibility(Visibility.PUBLIC);

            // 持久化用户实体
            try
            {
                if (iUserMapper.insert(userPO) != 1)
                {
                    throw new DataAccessException("用户数据插入失败") {};
                }
            }
            catch (DuplicateKeyException e)
            {
                log.warn("重复注册尝试 手机号: {}", registerRequest.getMobile());
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("手机号已被注册");
                return response;
            }

            // 设置用户上下文
            UserContext.setUserId(userPO.getId());

            // 构建设备实体
            DevicePO devicePO = new DevicePO();
            devicePO.setDeviceId(generateDeviceId(userPO.getId(), registerRequest.getPlatform().name()));
            devicePO.setUserId(userPO.getId());
            devicePO.setPlatform(registerRequest.getPlatform());
            devicePO.setClientVersion(registerRequest.getClientVersion());
            devicePO.setLastOnline(LocalDateTime.now());
            devicePO.setOnlineStatus(OnlineStatus.ONLINE);
            devicePO.setIpGeo(ipGeoParserUtils.parse(ipGeoParserUtils.getClientIp(httpRequest)));
            //devicePO.setIpGeo("");

            // 持久化设备实体
            try
            {
                if (iDeviceMapper.insert(devicePO) != 1)
                {
                    throw new DataAccessException("设备数据插入失败") {};
                }
            }
            catch (DuplicateKeyException e)
            {
                log.error("设备ID冲突: {}", devicePO.getDeviceId());
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("设备注册冲突");
                return response;
            }

            // 通过feign调用Gateway的JWT接口来生成JWT（事务外处理）
            String token;
            try
            {
                //token = gatewayFeignClient.generateJwt(userPO.getId());
                token = jwtUtil.generateJwt(UserContext.getUserId());
            }
            catch (Exception e)
            {
                log.error("JWT生成失败: {}", e.getMessage());
                response.setCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
                response.setMessage("系统服务暂时不可用");
                return response;
            }

            // 构建返回值
            UserBaseDTO userInfo = buildUserBaseDTO(userPO);
            userInfo.setCreatedAt(LocalDateTime.now());
            userInfo.setUpdatedAt(LocalDateTime.now());

            DeviceLoginDTO deviceInfo = buildDeviceLoginDTO(devicePO);
            deviceInfo.setLastOnline(LocalDateTime.now());

            response.setCode(HttpStatus.SC_CREATED);  // 201 Created更符合REST规范
            response.setData(new LoginResponse(token, userInfo, deviceInfo));
            response.setMessage("注册成功");

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

    // 更新用户 非敏感 信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<SearchResponse> update(UpdateRequest updateRequest, HttpServletRequest httpRequest)
    {
        BaseResponse<SearchResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            // 1. 参数基础校验
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            // 2. 查询现有用户数据
            UserPO userPO = iUserMapper.selectById(UserContext.getUserId());
            if (userPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            // 3. 构建用户实体类
            userPO.setNickname(updateRequest.getNickname());
            userPO.setAvatarUrl(updateRequest.getAvatarUrl());
            userPO.setVisibility(updateRequest.getVisibility());
            userPO.setStatus(updateRequest.getStatus());

            // 4. 持久化用户实体类
            try
            {
                if (iUserMapper.updateById(userPO) != 1)
                {
                    throw new DataAccessException("用户信息更新失败") {};
                }
            }
            catch (DuplicateKeyException e)
            {
                log.error("唯一性冲突: {}", e.getMessage());
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("数据冲突，请检查输入内容");
                return response;
            }

            // 5. 构建设备实体类
            DevicePO devicePO = new DevicePO();
            devicePO.setDeviceId(updateRequest.getDeviceId());
            devicePO.setUserId(UserContext.getUserId());
            devicePO.setClientVersion(updateRequest.getClientVersion());
            devicePO.setLastOnline(updateRequest.getLastOnline());
            devicePO.setOnlineStatus(updateRequest.getOnlineStatus());

            // 6. 持久化设备实体类
            try
            {
                if (iDeviceMapper.updateById(devicePO) != 1)
                {
                    throw new DataAccessException("设备信息更新失败") {};
                }
            }
            catch (DuplicateKeyException e)
            {
                log.error("唯一性冲突: {}", e.getMessage());
                response.setCode(HttpStatus.SC_CONFLICT);
                response.setMessage("数据冲突，请检查输入内容");
                return response;
            }

            // 7. 获取更新后的完整数据
            UserPO updatedUser = iUserMapper.selectById(UserContext.getUserId());
            DevicePO updatedDevice = iDeviceMapper.selectById(updateRequest.getDeviceId());
            DeviceBaseDTO updatedDeviceDTO = buildDeviceBaseDTO(updatedDevice);
            UserBaseDTO updatedUserDTO = buildUserBaseDTO(updatedUser);

            // 8. 构建返回结果
            SearchResponse searchResponse = new SearchResponse(updatedUserDTO, updatedDeviceDTO);

            response.setCode(HttpStatus.SC_OK);
            response.setMessage("信息更新成功");
            response.setData(searchResponse);

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
    public BaseResponse<SearchResponse> search(String deviceId)
    {
        BaseResponse<SearchResponse> response = new BaseResponse<>();
        response.setTimestamp(LocalDateTime.now());

        try
        {
            if (UserContext.getUserId() == null)
            {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setMessage("用户ID不能为空");
                return response;
            }

            UserPO userPO = iUserMapper.selectById(UserContext.getUserId());
            if (userPO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("用户不存在");
                return response;
            }

            DevicePO devicePO = iDeviceMapper.selectById(deviceId);
            if (devicePO == null)
            {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                response.setMessage("设备不存在");
                return response;
            }

            SearchResponse searchResponse = new SearchResponse(buildUserBaseDTO(userPO), buildDeviceBaseDTO(devicePO));

            response.setCode(HttpStatus.SC_OK);
            response.setData(searchResponse);
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

    // 构建用户信息DTO
    private UserBaseDTO buildUserBaseDTO(UserPO userPO)
    {
        UserBaseDTO dto = new UserBaseDTO();
        dto.setId(userPO.getId());
        dto.setMobile(userPO.getMobileEncrypted());
        dto.setNickname(userPO.getNickname());
        dto.setAvatarUrl(userPO.getAvatarUrl());
        dto.setStatus(userPO.getStatus());
        dto.setVisibility(userPO.getVisibility());
        dto.setCreatedAt(userPO.getCreatedAt());
        dto.setUpdatedAt(userPO.getUpdatedAt());
        return dto;
    }

    private DeviceBaseDTO buildDeviceBaseDTO(DevicePO devicePO)
    {
        DeviceBaseDTO dto = new DeviceBaseDTO();
        dto.setPlatform(devicePO.getPlatform());
        dto.setClientVersion(devicePO.getClientVersion());
        dto.setLastOnline(devicePO.getLastOnline());
        dto.setOnlineStatus(devicePO.getOnlineStatus());
        dto.setIpGeo(devicePO.getIpGeo());
        return dto;
    }

    // 构建设备信息DTO
    private DeviceLoginDTO buildDeviceLoginDTO(DevicePO devicePO)
    {
        DeviceLoginDTO dto = new DeviceLoginDTO();
        dto.setDeviceId(devicePO.getDeviceId());
        dto.setPlatform(devicePO.getPlatform());
        dto.setClientVersion(devicePO.getClientVersion());
        dto.setLastOnline(devicePO.getLastOnline());
        dto.setOnlineStatus(devicePO.getOnlineStatus());
        dto.setIpGeo(devicePO.getIpGeo());
        return dto;
    }

    // 暂时使用这个函数来代替真正的设备指纹生成业务
    private String generateDeviceId(Long userId, String platform)
    {
        return userId + " " + platform;
    }

    // 手机号格式验证
    private boolean isValidMobile(String mobile)
    {
        return mobile != null && mobile.matches("^\\+\\d{1,3}\\d{1,14}$");
    }

    // 密码强度校验
    public boolean isPasswordStrong(String password)
    {
        return password.length() >= 8
                && password.matches(".*[A-Za-z].*")
                && password.matches(".*\\d.*");
    }
}
