package com.atguigu.common.exception;

public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败");

    private int code;
    private String mesaage;

    public int getCode() {
        return code;
    }

    public String getMesaage() {
        return mesaage;
    }

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.mesaage = msg;
    }
}
