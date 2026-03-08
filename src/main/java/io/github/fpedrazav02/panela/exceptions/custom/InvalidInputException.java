package io.github.fpedrazav02.panela.exceptions.custom;

import io.github.fpedrazav02.panela.exceptions.PanelaException;

public class InvalidInputException extends PanelaException {
    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}

