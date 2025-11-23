package io.github.fpedrazav02.panela.utils;

import java.util.Optional;

public class Result<T> {

    private final boolean success;
    private final String error;
    private final T value;

    private Result(boolean success, String error, T value) {
        this.success = success;
        this.error = error;
        this.value = value;
    }

    public static <T> Result<T> ok(T value) {
        return new Result<>(true, null, value);
    }

    public static <T> Result<T> fail(String error) {
        return new Result<>(false, error, null);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public Optional<String> getError() {
        return Optional.ofNullable(this.error);
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(this.value);
    }

}
