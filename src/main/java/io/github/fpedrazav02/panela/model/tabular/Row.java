package io.github.fpedrazav02.panela.model.tabular;

import java.util.*;

public record Row(Map<String, String> values) {

    public Row {
        Objects.requireNonNull(values, "values");
        values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public String get(String column) {
        return values.get(column);
    }

    public Row with(String column, String value) {
        var copy = new LinkedHashMap<>(values);
        copy.put(column, value);
        return new Row(copy);
    }

    public Row drop(String column) {
        if (!values.containsKey(column)) return this;
        var copy = new LinkedHashMap<>(values);
        copy.remove(column);
        return new Row(copy);
    }
}