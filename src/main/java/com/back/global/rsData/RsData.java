package com.back.global.rsData;

public record RsData<T>(
        String resultCode,
        String msg,
        T data
) {

    public RsData(String resultCode, String msg) {
        this(
                resultCode,
                msg,
                null
        );
    }
}
