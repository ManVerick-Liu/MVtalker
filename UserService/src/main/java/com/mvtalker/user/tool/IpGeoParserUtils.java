package com.mvtalker.user.tool;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class IpGeoParserUtils
{
    @Value("${ip2location.db-path}")
    private String dbPath;

    private final IP2Location loc = new IP2Location();

    @PostConstruct // 使用构造后初始化
    public void init() {
        try (InputStream inputStream = new ClassPathResource(dbPath).getInputStream()) {
            // 创建临时文件
            Path tempFile = Files.createTempFile("ipdb", ".bin");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 使用官方推荐方式加载
            loc.Open(tempFile.toString(), true);
        } catch (Exception e) {
            log.error("IP解析器初始化失败", e);
            throw new RuntimeException("IP数据库初始化异常", e);
        }
    }

    private void handleOpenError(int resultCode) {
        String errorMsg;
        switch (resultCode) {
            case 1:
                errorMsg = "数据库文件缺失";
                break;
            case 2:
                errorMsg = "数据库损坏或格式错误";
                break;
            case 3:
                errorMsg = "不支持的数据库类型";
                break;
            case 4:
                errorMsg = "无效的IP地址格式";
                break;
            default:
                errorMsg = "未知错误";
        }
        log.error("IP数据库加载失败，错误码：{} - {}", resultCode, errorMsg);
        throw new RuntimeException("IP数据库加载失败：" + errorMsg);
    }

    // 添加同步锁（如果多线程访问）
    public synchronized String parse(String ip) {
        try {
            IPResult rec = loc.IPQuery(ip);

            switch (rec.getStatus()) {
                case "OK":
                    return String.format("%s-%s-%s",
                            rec.getCountryShort(),
                            rec.getRegion(),
                            rec.getCity());
                case "EMPTY_IP_ADDRESS":
                    log.warn("IP地址不能为空");
                    break;
                case "INVALID_IP_ADDRESS":
                    log.warn("无效的IP地址：{}", ip);
                    break;
                case "IPV6_NOT_SUPPORTED":
                    log.warn("IPv6地址不支持：{}", ip);
                    break;
                default:
                    log.warn("IP解析失败：{} - {}", ip, rec.getStatus());
            }
        } catch (Exception e) {
            log.error("IP解析异常：{}", ip, e);
        }
        return "Unknown";
    }


    @PreDestroy // 这个注解会在对象被销毁前调用其注释的方法
    public void destroy()
    {
        loc.Close();
        log.info("IP数据库连接已释放");
    }

    public String getClientIp(HttpServletRequest request)
    {
        // 处理代理服务器场景
        String[] headers =
                {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        String ip = null;
        for (String header : headers)
        {
            ip = request.getHeader(header);
            if (isValidIp(ip)) break;
        }

        // 直接获取客户端IP
        if (!isValidIp(ip))
        {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况（取第一个）
        return ip != null ? ip.split(",")[0].trim() : "0.0.0.0";
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
