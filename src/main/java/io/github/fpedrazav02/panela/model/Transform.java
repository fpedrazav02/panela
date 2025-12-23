package io.github.fpedrazav02.panela.model;

import java.util.Map;

public record Transform(
        String name,
        String type,
        String from,
        String script,
        String className,
        Map<String, Object> config
) {
}