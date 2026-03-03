package io.github.fpedrazav02.panela.runner.transform;

import io.github.fpedrazav02.panela.model.Transform;
import io.github.fpedrazav02.panela.model.tabular.Row;
import io.github.fpedrazav02.panela.model.tabular.Table;

import java.util.*;

public final class TableTransformRunner implements TransformRunner {

    @Override
    public Object execute(Transform transform, Object inputData) {
        if (!(inputData instanceof Table table)) {
            throw new IllegalArgumentException("table transform expects Table input, got: " +
                    (inputData == null ? "null" : inputData.getClass().getName()));
        }

        System.out.println("DEBUG transform.config=" + transform.config());

        String op = (String) transform.config().get("op");
        if (op == null || op.isBlank()) {
            throw new IllegalArgumentException("Missing op for table transform: " + transform.name());
        }

        // refactor to data outside object
        return switch (op) {
            case "trim_fields" -> trimFields(table);
            case "drop_columns" -> dropColumns(table, transform.config());
            default -> throw new IllegalArgumentException("Unknown table op: " + op);
        };
    }

    private Table trimFields(Table t) {
        var newRows = t.rows().stream().map(r -> {
            var m = new LinkedHashMap<String, String>();
            for (String c : t.columns()) {
                String v = r.get(c);
                m.put(c, v == null ? null : v.trim());
            }
            return new Row(m);
        }).toList();
        return new Table(t.columns(), newRows);
    }

    @SuppressWarnings("unchecked")
    private Table dropColumns(Table t, Map<String, Object> cfg) {
        Object colsObj = cfg.get("columns");
        if (!(colsObj instanceof List<?> colsRaw) || colsRaw.isEmpty()) {
            throw new IllegalArgumentException("drop_columns requires config.columns = non-empty list");
        }

        boolean failIfMissing = cfg.get("failIfMissing") == null || (Boolean) cfg.get("failIfMissing");
        List<String> cols = colsRaw.stream().map(Object::toString).toList();

        for (String c : cols) {
            if (!t.hasColumn(c) && failIfMissing) {
                throw new IllegalArgumentException("Column not found: " + c);
            }
        }

        List<String> newCols = t.columns().stream().filter(c -> !cols.contains(c)).toList();
        var newRows = t.rows().stream().map(r -> {
            Row rr = r;
            for (String c : cols) rr = rr.drop(c);
            return rr;
        }).toList();

        return new Table(newCols, newRows);
    }

    @Override
    public String getType() {
        return "table";
    }
}