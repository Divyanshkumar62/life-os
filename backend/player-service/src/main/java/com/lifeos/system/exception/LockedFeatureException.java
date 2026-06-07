package com.lifeos.system.exception;

public class LockedFeatureException extends IllegalStateException {
    public LockedFeatureException(String message) {
        super(message);
    }
}
