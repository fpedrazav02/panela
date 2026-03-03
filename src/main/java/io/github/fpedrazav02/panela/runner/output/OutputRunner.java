package io.github.fpedrazav02.panela.runner.output;

import io.github.fpedrazav02.panela.model.Output;

public interface OutputRunner {
    void execute(Output output, Object inputData, String jobName) throws Exception;
    String getType();
}