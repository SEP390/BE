package com.capstone.capstone.dto.response;

import lombok.*;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BaseResponse<T> {
    private int status;
    private String message;
    private T data;

    public BaseResponse(T data) {
        this.status = HttpStatus.OK.value();
        this.message = "success";
        this.data = data;
    }
}
