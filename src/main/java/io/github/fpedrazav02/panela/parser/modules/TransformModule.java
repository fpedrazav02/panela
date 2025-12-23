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
        module.set("echo", new EchoFunction());

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