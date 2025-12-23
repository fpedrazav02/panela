package io.github.fpedrazav02.panela.runner.transform;

import io.github.fpedrazav02.panela.model.Transform;

public class EchoTransformRunner implements TransformRunner {

    @Override
    public Object execute(Transform transform, Object inputData) {
        return inputData;
    }

    @Override
    public String getType() {
        return "echo";
    }
}