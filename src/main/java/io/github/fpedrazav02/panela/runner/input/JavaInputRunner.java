package io.github.fpedrazav02.panela.runner.input;

import io.github.fpedrazav02.panela.model.Input;

public class JavaInputRunner implements InputRunner {

    @Override
    public Object execute(Input input) throws Exception {
        throw new UnsupportedOperationException("Java inputs not implemented yet");
    }

    @Override
    public String getType() {
        return "java";
    }
}