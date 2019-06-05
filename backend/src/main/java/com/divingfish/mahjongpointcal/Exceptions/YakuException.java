package com.divingfish.mahjongpointcal.Exceptions;

public class YakuException extends Exception {
    String message;

    public YakuException(String m) {
        message = m;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
