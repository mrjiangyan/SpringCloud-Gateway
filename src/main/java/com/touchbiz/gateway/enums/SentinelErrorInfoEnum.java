package com.touchbiz.gateway.enums;

public enum SentinelErrorInfoEnum {
    FlowException("访问频繁，请稍候再试"),
    ParamFlowException("热点参数限流"),
    SystemBlockException("系统规则限流或降级"),
    AuthorityException("授权规则不通过"),
    UnknownError("未知异常"),
    DegradeException("服务降级");

    String error;
    Integer code;

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    SentinelErrorInfoEnum(String error, Integer code) {
        this.error = error;
        this.code = code;
    }

    SentinelErrorInfoEnum(String error) {
        this.error = error;
        this.code = 500;
    }

    public static SentinelErrorInfoEnum getErrorByException(Throwable throwable) {
        if (throwable == null) {
            return null;
        } else {
            String exceptionClass = throwable.getClass().getSimpleName();
            SentinelErrorInfoEnum[] var2 = values();
            int var3 = var2.length;

            for (SentinelErrorInfoEnum e : var2) {
                if (exceptionClass.equals(e.name())) {
                    return e;
                }
            }

            return null;
        }
    }
}