package com.niuwa.streaming.settings;


import com.niuwa.streaming.exception.SettingsException;

/**
 * BlogInfo: william
 * Date: 11-9-1
 * Time: 下午4:37
 */
public class NoClassSettingsException extends SettingsException {

    public NoClassSettingsException(String message) {
        super(message);
    }

    public NoClassSettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
