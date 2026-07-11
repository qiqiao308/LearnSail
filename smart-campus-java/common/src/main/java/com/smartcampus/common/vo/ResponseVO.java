package com.smartcampus.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseVO<T> {

    private String status;

    private Integer code;

    private String info;

    private T data;

    public static <T> ResponseVO<T> success(T data) {
        return new ResponseVO<>("success", 200, "成功", data);
    }

    public static <T> ResponseVO<T> success(String info, T data) {
        return new ResponseVO<>("success", 200, info, data);
    }

    public static <T> ResponseVO<T> error(Integer code, String info) {
        return new ResponseVO<>("error", code, info, null);
    }

    public static <T> ResponseVO<T> error(String info) {
        return new ResponseVO<>("error", 500, info, null);
    }
}
