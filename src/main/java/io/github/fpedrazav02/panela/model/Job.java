package io.github.fpedrazav02.panela.model;

import java.util.List;

public record Job(
        String name,
        String version,
        List<Input> inputs,
        List<Transform> transforms,
        List<Output> outputs
) {
}