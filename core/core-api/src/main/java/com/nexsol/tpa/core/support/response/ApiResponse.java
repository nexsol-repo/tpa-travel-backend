package com.nexsol.tpa.core.support.response;

import com.nexsol.tpa.core.support.error.CoreApiErrorMessage;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {

    private final ResultType result;

    private final T data;

    private final CoreApiErrorMessage error;

    public static ApiResponse<Object> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }

    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static <S> ApiResponse<S> error(CoreApiErrorType error, Object errorData) {
        return new ApiResponse<>(ResultType.ERROR, null, new CoreApiErrorMessage(error, errorData));
    }

    public static <S> ApiResponse<S> error(CoreApiErrorType error) {
        return new ApiResponse<>(ResultType.ERROR, null, new CoreApiErrorMessage(error, null));
    }
}
