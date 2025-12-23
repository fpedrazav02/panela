package io.github.fpedrazav02.panela.model;

import java.util.Map;

public record Input(
        String name,
        String type,
        String script,
        String className,
        Map<String, Object> config
) {
}