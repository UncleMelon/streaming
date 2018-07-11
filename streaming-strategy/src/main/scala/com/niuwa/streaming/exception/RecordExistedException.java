package com.niuwa.streaming.exception;


public class RecordExistedException extends RuntimeException {
    public RecordExistedException(String message) {
        super(message);
    }
}
