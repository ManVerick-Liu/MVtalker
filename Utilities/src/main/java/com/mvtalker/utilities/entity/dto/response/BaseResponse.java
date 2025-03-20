package com.mvtalker.utilities.entity.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseResponse<T>
{
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
