package io.github.fpedrazav02.panela.exceptions;

public class PanelaException extends Exception {

    public PanelaException(String message) {
        super(message);
    }

    public PanelaException(String message, Throwable cause) {
        super(message, cause);
    }
}
