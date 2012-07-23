package com.tuppari;

public class TuppariException extends RuntimeException {

    public TuppariException() {
    }

    public TuppariException(String s) {
        super(s);
    }

    public TuppariException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TuppariException(Throwable throwable) {
        super(throwable);
    }
}
