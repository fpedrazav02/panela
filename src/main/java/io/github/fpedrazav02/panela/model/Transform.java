package io.github.fpedrazav02.panela.model;

import java.util.Map;

public record Transform(
        String name,
        String function,
        String from,
        Map<String, String> params
) {
}