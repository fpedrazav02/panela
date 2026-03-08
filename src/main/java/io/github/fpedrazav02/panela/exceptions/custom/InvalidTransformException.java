package io.github.fpedrazav02.panela.exceptions.custom;

import io.github.fpedrazav02.panela.exceptions.PanelaException;

public class InvalidTransformException extends PanelaException {
    public InvalidTransformException(String message) {
        super(message);
    }

    public InvalidTransformException(String message, Throwable cause) {
        super(message, cause);
    }
}

