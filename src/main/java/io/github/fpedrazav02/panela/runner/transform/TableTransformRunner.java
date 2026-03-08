package io.github.fpedrazav02.panela.runner.transform;

import io.github.fpedrazav02.panela.exceptions.custom.InvalidTransformException;
import io.github.fpedrazav02.panela.model.Transform;
import io.github.fpedrazav02.panela.model.tabular.Row;
import io.github.fpedrazav02.panela.model.tabular.Table;

import java.util.*;

public final class TableTransformRunner implements TransformRunner {

    @Override
    public Object execute(Transform transform, Object inputData) throws InvalidTransformException {
        if (!(inputData instanceof Table table)) {
            throw new InvalidTransformException(
                    "Transform '" + transform.name() + "' expects Table input, got: "
                    + (inputData == null ? "null" : inputData.getClass().getSimpleName()));
        }

        String op = (String) transform.config().get("op");
        if (op == null || op.isBlank()) {
            throw new InvalidTransformException(
                    "Transform '" + transform.name() + "': missing required config field 'op'");
        }

        return switch (op) {
            case "trim_fields"    -> trimFields(table);
            case "drop_columns"   -> dropColumns(transform.name(), table, transform.config());
            case "filter_rows"    -> filterRows(transform.name(), table, transform.config());
            case "select_columns" -> selectColumns(transform.name(), table, transform.config());
            case "rename_columns" -> renameColumns(transform.name(), table, transform.config());
            case "add_column"     -> addColumn(transform.name(), table, transform.config());
            case "sort_rows"      -> sortRows(transform.name(), table, transform.config());
            case "deduplicate"    -> deduplicate(transform.name(), table, transform.config());
            case "fill_nulls"     -> fillNulls(transform.name(), table, transform.config());
            case "cast_column"    -> castColumn(transform.name(), table, transform.config());
            default -> throw new InvalidTransformException(
                    "Transform '" + transform.name() + "': unknown op '" + op + "'");
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

    private Table dropColumns(String name, Table t, Map<String, Object> cfg) throws InvalidTransformException {
        Object colsObj = cfg.get("columns");
        if (!(colsObj instanceof List<?> colsRaw) || colsRaw.isEmpty()) {
            throw new InvalidTransformException(
                    "Transform '" + name + "' (drop_columns): 'columns' must be a non-empty list");
        }

        boolean failIfMissing = cfg.get("failIfMissing") == null || (Boolean) cfg.get("failIfMissing");
        List<String> cols = colsRaw.stream().map(Object::toString).toList();

        for (String c : cols) {
            if (!t.hasColumn(c) && failIfMissing) {
                throw new InvalidTransformException(
                        "Transform '" + name + "' (drop_columns): column '" + c + "' not found");
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

    // ------------------------------------------------------------------ //
    //  New operations                                                      //
    // ------------------------------------------------------------------ //

    private Table filterRows(String name, Table t, Map<String, Object> cfg) throws InvalidTransformException {
        String col   = requireString(name, cfg, "column");
        String value = requireString(name, cfg, "value");
        String cmpOp = cfg.getOrDefault("cmp", "eq").toString();

        if (!t.hasColumn(col)) {
            throw new InvalidTransformException(
                    "Transform '" + name + "' (filter_rows): column '" + col + "' not found");
        }

        var newRows = t.rows().stream().filter(r -> {
            String cell = r.get(col);
            if (cell == null) return false;
            return switch (cmpOp) {
                case "eq"          -> cell.equals(value);
                case "neq"         -> !cell.equals(value);
                case "contains"    -> cell.contains(value);
                case "starts_with" -> cell.startsWith(value);
                case "ends_with"   -> cell.endsWith(value);
                case "gt"          -> parseDouble(cell) > parseDouble(value);
                case "lt"          -> parseDouble(cell) < parseDouble(value);
                default -> throw new RuntimeException(
                        "Transform '" + name + "' (filter_rows): unknown op '" + cmpOp
                        + "' — expected eq, neq, contains, starts_with, ends_with, gt, lt");
            };
        }).toList();

        return new Table(t.columns(), newRows);
    }

    private Table selectColumns(String name, Table t, Map<String, Object> cfg) throws InvalidTransformException {
        Object colsObj = cfg.get("columns");
        if (!(colsObj instanceof List<?> colsRaw) || colsRaw.isEmpty()) {
            throw new InvalidTransformException(
                    "Transform '" + name + "' (select_columns): 'columns' must be a non-empty list");
        }
        List<String> cols = colsRaw.stream().map(Object::toString).toList();
        for (String c : cols) {
            if (!t.hasColumn(c))
                throw new InvalidTransformException(
                        "Transform '" + name + "' (select_columns): column '" + c + "' not found");
        }
        var newRows = t.rows().stream().map(r -> {
            var m = new LinkedHashMap<String, String>();
            for (String c : cols) m.put(c, r.get(c));
            return new Row(m);
        }).toList();
        return new Table(cols, newRows);
    }

    private Table renameColumns(String name, Table t, Map<String, Object> cfg) throws InvalidTransformException {
        Object renameObj = cfg.get("rename");
        if (!(renameObj instanceof Map<?, ?> renameRaw) || renameRaw.isEmpty()) {
            throw new InvalidTransformException(
                    "Transform '" + name + "' (rename_columns): 'rename' must be a non-empty map");
        }
        Map<String, String> rename = new LinkedHashMap<>();
        renameRaw.forEach((k, v) -> rename.put(k.toString(), v.toString()));

        for (String old : rename.keySet()) {
            if (!t.hasColumn(old))
                throw new InvalidTransformException(
                        "Transform '" + name + "' (rename_columns): column '" + old + "' not found");
        }

        List<String> newCols = t.columns().stream().map(c -> rename.getOrDefault(c, c)).toList();
        var newRows = t.rows().stream().map(r -> {
            var m = new LinkedHashMap<String, String>();
            for (String c : t.columns()) m.put(rename.getOrDefault(c, c), r.get(c));
            return new Row(m);
        }).toList();
        return new Table(newCols, newRows);
    }

    private Table addColumn(String name, Table t, Map<String, Object> cfg) throws InvalidTransformException {
        String newCol  = requireString(name, cfg, "name");
        String constVal = cfg.containsKey("value") ? cfg.get("value").toString() : null;
        String fromCol = cfg.containsKey("from_column") ? cfg.get("from_column").toString() : null;
        String expr    = cfg.getOrDefault("expression", "copy").toString();

        if (t.hasColumn(newCol)) {
            throw new InvalidTransformException(
                    "Transform '" + name + "' (add_column): column '" + newCol + "' already exists");
        }
        if (fromCol != null && !t.hasColumn(fromCol)) {
            throw new InvalidTransformException(
                    "Transform '" + name + "' (add_column): from_column '" + fromCol + "' not found");
        }

        List<String> newCols = new ArrayList<>(t.columns());
        newCols.add(newCol);

        final String resolvedFromCol = fromCol;
        var newRows = t.rows().stream().map(r -> {
            String computed;
            if (constVal != null) {
                computed = constVal;
            } else if (resolvedFromCol != null) {
                String src = r.get(resolvedFromCol);
                computed = src == null ? null : switch (expr) {
                    case "copy"   -> src;
                    case "upper"  -> src.toUpperCase();
                    case "lower"  -> src.toLowerCase();
                    case "length" -> String.valueOf(src.length());
                    default -> throw new RuntimeException(
                            "Transform '" + name + "' (add_column): unknown expression '" + expr
                            + "' — expected copy, upper, lower, length");
                };
            } else {
                computed = null;
            }
            return r.with(newCol, computed);
        }).toList();

        return new Table(newCols, newRows);
    }

    private Table sortRows(String name, Table t, Map<String, Object> cfg) throws InvalidTransformException {
        String col     = requireString(name, cfg, "column");
        String order   = cfg.getOrDefault("order", "asc").toString();
        boolean numeric = Boolean.parseBoolean(cfg.getOrDefault("numeric", "false").toString());

        if (!t.hasColumn(col))
            throw new InvalidTransformException(
                    "Transform '" + name + "' (sort_rows): column '" + col + "' not found");

        Comparator<Row> cmp = numeric
                ? Comparator.comparingDouble(r -> parseDoubleOrMin(r.get(col)))
                : Comparator.comparing(r -> nullToEmpty(r.get(col)));

        if ("desc".equalsIgnoreCase(order)) cmp = cmp.reversed();
        return new Table(t.columns(), t.rows().stream().sorted(cmp).toList());
    }

    private Table deduplicate(String name, Table t, Map<String, Object> cfg) throws InvalidTransformException {
        List<String> keyCols;
        Object colsObj = cfg.get("columns");
        if (colsObj instanceof List<?> raw && !raw.isEmpty()) {
            keyCols = raw.stream().map(Object::toString).toList();
            for (String c : keyCols) {
                if (!t.hasColumn(c))
                    throw new InvalidTransformException(
                            "Transform '" + name + "' (deduplicate): column '" + c + "' not found");
            }
        } else {
            keyCols = t.columns();
        }

        Set<String> seen = new LinkedHashSet<>();
        var newRows = new ArrayList<Row>();
        for (Row r : t.rows()) {
            StringBuilder key = new StringBuilder();
            for (String c : keyCols) key.append(c).append("=").append(nullToEmpty(r.get(c))).append("|");
            if (seen.add(key.toString())) newRows.add(r);
        }
        return new Table(t.columns(), newRows);
    }

    private Table fillNulls(String name, Table t, Map<String, Object> cfg) throws InvalidTransformException {
        String fill = requireString(name, cfg, "value");

        List<String> targetCols;
        Object colsObj = cfg.get("columns");
        if (colsObj instanceof List<?> raw && !raw.isEmpty()) {
            targetCols = raw.stream().map(Object::toString).toList();
            for (String c : targetCols) {
                if (!t.hasColumn(c))
                    throw new InvalidTransformException(
                            "Transform '" + name + "' (fill_nulls): column '" + c + "' not found");
            }
        } else {
            targetCols = t.columns();
        }

        var newRows = t.rows().stream().map(r -> {
            Row rr = r;
            for (String c : targetCols) {
                String v = rr.get(c);
                if (v == null || v.isEmpty()) rr = rr.with(c, fill);
            }
            return rr;
        }).toList();
        return new Table(t.columns(), newRows);
    }

    private Table castColumn(String name, Table t, Map<String, Object> cfg) throws InvalidTransformException {
        String col      = requireString(name, cfg, "column");
        String castType = cfg.getOrDefault("type", "string").toString();

        if (!t.hasColumn(col))
            throw new InvalidTransformException(
                    "Transform '" + name + "' (cast_column): column '" + col + "' not found");

        var newRows = t.rows().stream().map(r -> {
            String v = r.get(col);
            if (v == null) return r;
            try {
                String casted = switch (castType) {
                    case "integer"   -> String.valueOf((long) Double.parseDouble(v.trim()));
                    case "float"     -> String.valueOf(Double.parseDouble(v.trim()));
                    case "boolean"   -> String.valueOf(Boolean.parseBoolean(v.trim()));
                    case "uppercase" -> v.toUpperCase();
                    case "lowercase" -> v.toLowerCase();
                    case "string"    -> v;
                    default -> throw new RuntimeException(
                            "Transform '" + name + "' (cast_column): unknown type '" + castType
                            + "' — expected integer, float, boolean, uppercase, lowercase, string");
                };
                return r.with(col, casted);
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        "Transform '" + name + "' (cast_column): cannot cast value '" + v
                        + "' to " + castType + " in column '" + col + "'");
            }
        }).toList();

        return new Table(t.columns(), newRows);
    }

    // ------------------------------------------------------------------ //
    //  Utilities                                                           //
    // ------------------------------------------------------------------ //

    private static String requireString(String transformName, Map<String, Object> cfg, String key)
            throws InvalidTransformException {
        Object v = cfg.get(key);
        if (v == null || v.toString().isBlank()) {
            throw new InvalidTransformException(
                    "Transform '" + transformName + "': missing required config field '" + key + "'");
        }
        return v.toString();
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) {
            throw new RuntimeException("Expected a numeric value, got: '" + s + "'");
        }
    }

    private static double parseDoubleOrMin(String s) {
        if (s == null) return Double.NEGATIVE_INFINITY;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return Double.NEGATIVE_INFINITY; }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    @Override
    public String getType() { return "table"; }
}

