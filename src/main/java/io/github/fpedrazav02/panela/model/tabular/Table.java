package io.github.fpedrazav02.panela.model.tabular;

import java.util.*;

public record Table(List<String> columns, List<Row> rows) {

    public Table {
        Objects.requireNonNull(columns, "columns");
        Objects.requireNonNull(rows, "rows");

        columns = List.copyOf(columns);
        rows = List.copyOf(rows);

        Set<String> seen = new HashSet<>();
        for (String c : columns) {
            if (c == null || c.isBlank()) throw new IllegalArgumentException("Blank column name");
            if (!seen.add(c)) throw new IllegalArgumentException("Duplicate column: " + c);
        }
    }

    public boolean hasColumn(String col) {
        return columns.contains(col);
    }

    public int rowCount() { return rows.size(); }
    public int colCount() { return columns.size(); }
}