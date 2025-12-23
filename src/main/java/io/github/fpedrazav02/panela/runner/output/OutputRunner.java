package io.github.fpedrazav02.panela.runner.output;

import io.github.fpedrazav02.panela.model.Output;

public interface OutputRunner {
    void execute(Output output, Object inputData) throws Exception;
    String getType();
}