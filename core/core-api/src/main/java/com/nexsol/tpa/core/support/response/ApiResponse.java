package com.nexsol.tpa.core.support.response;

import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.support.error.CoreApiErrorMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
//@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static <S> ApiResponse<S> error(CoreErrorType error, Object errorData) {
        return new ApiResponse<>(ResultType.ERROR, null, new CoreApiErrorMessage(error, errorData));
    }

    public static <S> ApiResponse<S> error(CoreErrorType error) {
        return new ApiResponse<>(ResultType.ERROR, null, new CoreApiErrorMessage(error, null));
    }

}