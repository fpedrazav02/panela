package io.github.fpedrazav02.panela.exceptions.custom;

import io.github.fpedrazav02.panela.exceptions.PanelaException;

public class InvalidOutputException extends PanelaException {
    public InvalidOutputException(String message) {
        super(message);
    }

    public InvalidOutputException(String message, Throwable cause) {
        super(message, cause);
    }
}

