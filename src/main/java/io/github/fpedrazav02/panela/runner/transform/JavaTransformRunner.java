package io.github.fpedrazav02.panela.runner.transform;

import io.github.fpedrazav02.panela.model.Transform;

public class JavaTransformRunner implements TransformRunner {

    @Override
    public Object execute(Transform transform, Object inputData) throws Exception {
        throw new UnsupportedOperationException("Java transforms not implemented yet");
    }

    @Override
    public String getType() {
        return "java";
    }
}