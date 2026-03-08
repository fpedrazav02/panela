package io.github.fpedrazav02.panela.parser.modules;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class TransformModule extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable module = new LuaTable();

        // Builtin
        module.set("echo", new EchoFunction());

        // Builtin - TABLES
        module.set("trim",           new TrimFunction());
        module.set("drop_columns",   new DropColumnsFunction());
        module.set("filter_rows",    new FilterRowsFunction());
        module.set("select_columns", new SelectColumnsFunction());
        module.set("rename_columns", new RenameColumnsFunction());
        module.set("add_column",     new AddColumnFunction());
        module.set("sort_rows",      new SortRowsFunction());
        module.set("deduplicate",    new DeduplicateFunction());
        module.set("fill_nulls",     new FillNullsFunction());
        module.set("cast_column",    new CastColumnFunction());

        // Custom
        module.set("lua", new LuaTransformFunction());
        module.set("java", new JavaTransformFunction());

        return module;
    }

    static class EchoFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable config = args.checktable(1);

            // Create result table with metadata
            LuaTable result = new LuaTable();
            result.set("type", LuaValue.valueOf("echo"));
            result.set("function", LuaValue.valueOf("echo"));
            result.set("from", config.get("from"));
            result.set("params", config.get("params"));

            return result;
        }
    }

    static class TrimFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);

            LuaValue from = cfg.get("from");
            if (from.isnil()) {
                throw new IllegalArgumentException("transform.trim requires field: from");
            }

            LuaTable opConfig = new LuaTable();
            opConfig.set("op", LuaValue.valueOf("trim_fields"));

            LuaTable result = new LuaTable();
            result.set("type", LuaValue.valueOf("table"));
            result.set("from", from);
            result.set("config", opConfig);

            return result;
        }
    }

    static class DropColumnsFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);

            LuaValue from = cfg.get("from");
            if (from.isnil()) {
                throw new IllegalArgumentException("transform.drop_columns requires field: from");
            }

            LuaValue columns = cfg.get("columns");
            if (columns.isnil() || !columns.istable()) {
                throw new IllegalArgumentException("transform.drop_columns requires field: columns (table/array)");
            }

            LuaValue failIfMissing = cfg.get("failIfMissing"); // optional

            LuaTable opConfig = new LuaTable();
            opConfig.set("op", LuaValue.valueOf("drop_columns"));
            opConfig.set("columns", columns);
            if (!failIfMissing.isnil()) {
                opConfig.set("failIfMissing", failIfMissing);
            }

            LuaTable result = new LuaTable();
            result.set("type", LuaValue.valueOf("table"));
            result.set("from", from);
            result.set("config", opConfig);

            return result;
        }
    }

    /** filter_rows { from, column, value, op? } */
    static class FilterRowsFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);
            requireField(cfg, "from",   "transform.filter_rows");
            requireField(cfg, "column", "transform.filter_rows");
            requireField(cfg, "value",  "transform.filter_rows");

            LuaTable opConfig = new LuaTable();
            opConfig.set("op",     LuaValue.valueOf("filter_rows"));
            opConfig.set("column", cfg.get("column"));
            opConfig.set("value",  cfg.get("value"));
            // "op" in the Lua call is the comparison operator (eq, neq, contains, …)
            // stored under "cmp" to avoid shadowing the dispatch "op" key
            if (!cfg.get("op").isnil()) opConfig.set("cmp", cfg.get("op"));

            LuaTable result = new LuaTable();
            result.set("type",   LuaValue.valueOf("table"));
            result.set("from",   cfg.get("from"));
            result.set("config", opConfig);
            return result;
        }
    }

    /** select_columns { from, columns } */
    static class SelectColumnsFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);
            requireField(cfg, "from",    "transform.select_columns");
            requireField(cfg, "columns", "transform.select_columns");

            LuaTable opConfig = new LuaTable();
            opConfig.set("op",      LuaValue.valueOf("select_columns"));
            opConfig.set("columns", cfg.get("columns"));

            LuaTable result = new LuaTable();
            result.set("type",   LuaValue.valueOf("table"));
            result.set("from",   cfg.get("from"));
            result.set("config", opConfig);
            return result;
        }
    }

    /** rename_columns { from, rename } — rename is a Lua table { old = "new", ... } */
    static class RenameColumnsFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);
            requireField(cfg, "from",   "transform.rename_columns");
            requireField(cfg, "rename", "transform.rename_columns");

            LuaTable opConfig = new LuaTable();
            opConfig.set("op",     LuaValue.valueOf("rename_columns"));
            opConfig.set("rename", cfg.get("rename"));

            LuaTable result = new LuaTable();
            result.set("type",   LuaValue.valueOf("table"));
            result.set("from",   cfg.get("from"));
            result.set("config", opConfig);
            return result;
        }
    }

    /** add_column { from, name, value?, from_column?, expression? } */
    static class AddColumnFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);
            requireField(cfg, "from", "transform.add_column");
            requireField(cfg, "name", "transform.add_column");

            LuaTable opConfig = new LuaTable();
            opConfig.set("op",   LuaValue.valueOf("add_column"));
            opConfig.set("name", cfg.get("name"));
            if (!cfg.get("value").isnil())       opConfig.set("value",       cfg.get("value"));
            if (!cfg.get("from_column").isnil())  opConfig.set("from_column", cfg.get("from_column"));
            if (!cfg.get("expression").isnil())   opConfig.set("expression",  cfg.get("expression"));

            LuaTable result = new LuaTable();
            result.set("type",   LuaValue.valueOf("table"));
            result.set("from",   cfg.get("from"));
            result.set("config", opConfig);
            return result;
        }
    }

    /** sort_rows { from, column, order?, numeric? } */
    static class SortRowsFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);
            requireField(cfg, "from",   "transform.sort_rows");
            requireField(cfg, "column", "transform.sort_rows");

            LuaTable opConfig = new LuaTable();
            opConfig.set("op",     LuaValue.valueOf("sort_rows"));
            opConfig.set("column", cfg.get("column"));
            if (!cfg.get("order").isnil())   opConfig.set("order",   cfg.get("order"));
            if (!cfg.get("numeric").isnil()) opConfig.set("numeric", cfg.get("numeric"));

            LuaTable result = new LuaTable();
            result.set("type",   LuaValue.valueOf("table"));
            result.set("from",   cfg.get("from"));
            result.set("config", opConfig);
            return result;
        }
    }

    /** deduplicate { from, columns? } */
    static class DeduplicateFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);
            requireField(cfg, "from", "transform.deduplicate");

            LuaTable opConfig = new LuaTable();
            opConfig.set("op", LuaValue.valueOf("deduplicate"));
            if (!cfg.get("columns").isnil()) opConfig.set("columns", cfg.get("columns"));

            LuaTable result = new LuaTable();
            result.set("type",   LuaValue.valueOf("table"));
            result.set("from",   cfg.get("from"));
            result.set("config", opConfig);
            return result;
        }
    }

    /** fill_nulls { from, value, columns? } */
    static class FillNullsFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);
            requireField(cfg, "from",  "transform.fill_nulls");
            requireField(cfg, "value", "transform.fill_nulls");

            LuaTable opConfig = new LuaTable();
            opConfig.set("op",    LuaValue.valueOf("fill_nulls"));
            opConfig.set("value", cfg.get("value"));
            if (!cfg.get("columns").isnil()) opConfig.set("columns", cfg.get("columns"));

            LuaTable result = new LuaTable();
            result.set("type",   LuaValue.valueOf("table"));
            result.set("from",   cfg.get("from"));
            result.set("config", opConfig);
            return result;
        }
    }

    /** cast_column { from, column, type? } */
    static class CastColumnFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable cfg = args.checktable(1);
            requireField(cfg, "from",   "transform.cast_column");
            requireField(cfg, "column", "transform.cast_column");

            LuaTable opConfig = new LuaTable();
            opConfig.set("op",     LuaValue.valueOf("cast_column"));
            opConfig.set("column", cfg.get("column"));
            if (!cfg.get("type").isnil()) opConfig.set("type", cfg.get("type"));

            LuaTable result = new LuaTable();
            result.set("type",   LuaValue.valueOf("table"));
            result.set("from",   cfg.get("from"));
            result.set("config", opConfig);
            return result;
        }
    }

    private static void requireField(LuaTable cfg, String field, String ctx) {
        if (cfg.get(field).isnil()) {
            throw new IllegalArgumentException(ctx + " requires field: " + field);
        }
    }

    // Custom: transform.lua { script = "transform/kafka.lua", config = {...} }
    static class LuaTransformFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable config = args.checktable(1);

            LuaTable result = new LuaTable();
            result.set("type", LuaValue.valueOf("lua"));
            result.set("from", config.get("from"));
            result.set("script", config.get("script"));
            result.set("config", config.get("config"));

            return result;
        }
    }

    // Custom: transform.java { class = "...", config = {...} }
    static class JavaTransformFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable config = args.checktable(1);

            LuaTable result = new LuaTable();
            result.set("type", LuaValue.valueOf("java"));
            result.set("from", config.get("from"));
            result.set("class", config.get("class"));
            result.set("config", config.get("config"));

            return result;
        }
    }
}
