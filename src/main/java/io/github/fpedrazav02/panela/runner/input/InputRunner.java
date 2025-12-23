package io.github.fpedrazav02.panela.runner.input;

import io.github.fpedrazav02.panela.model.Input;

public interface InputRunner {
    Object execute(Input input) throws Exception;

    String getType();
}