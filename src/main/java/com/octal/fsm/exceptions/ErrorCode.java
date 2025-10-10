package com.octal.fsm.exceptions;

public enum ErrorCode {
    SUCCESS(200),
    INVALIDFORMAT(101),
    ACCOUNT_LOCKED(300),
    EMPTYFOUND(102),
    ALREADYFOUND(103),
    RECORDNOTFOUND(104),
    INVALIDUSERCREDENTIALS(105),
    TOKENEXPIRE(106),
    INVALIDTOKEN(107),
    INACTIVEUSER(108),
    DELETEUSER(109),
    EXCEPTION_OCCUR(110),
    COMMON(111),
    COMPLETE_REGISTRATION(112),
    RECORD_NOT_FOUND(101),
    TRANSACTION_UNDER_PROCESS(112),
    NEW_DEVICE_LOGGED_IN(222),
    ACCESS_NOT_GRANTED(401),
    BAD_REQUEST(400);

    int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
