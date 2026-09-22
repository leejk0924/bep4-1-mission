package com.back.global.rsData;

import com.back.standard.resultType.ResultType;

public record RsData<T> (
        String resultCode,
        String msg,
        T data
) implements ResultType {

    public RsData(String resultCode, String msg) {
        this(
                resultCode,
                msg,
                null
        );
    }
}
