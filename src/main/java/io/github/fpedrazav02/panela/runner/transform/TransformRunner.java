package io.github.fpedrazav02.panela.runner.transform;

import io.github.fpedrazav02.panela.model.Transform;

public interface TransformRunner {
    Object execute(Transform transform, Object inputData) throws Exception;
    String getType();
}