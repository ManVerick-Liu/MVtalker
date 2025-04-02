package com.mvtalker.utilities.entity.baseResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T>
{
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
