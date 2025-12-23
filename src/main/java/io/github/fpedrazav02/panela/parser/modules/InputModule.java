package io.github.fpedrazav02.panela.parser.modules;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class InputModule extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable module = new LuaTable();

        // Built-in
        module.set("value", new ValueFunction());

        // Custom
        module.set("lua", new LuaInputFunction());
        module.set("java", new JavaInputFunction());

        return module;
    }

    // Built-in: input.value
    static class ValueFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable config = args.checktable(1);

            LuaTable result = new LuaTable();
            result.set("type", LuaValue.valueOf("value"));
            result.set("config", config);

            return result;
        }
    }

    // Custom: input.lua { script = "inputs/kafka.lua", config = {...} }
    static class LuaInputFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable config = args.checktable(1);

            LuaTable result = new LuaTable();
            result.set("type", LuaValue.valueOf("lua"));
            result.set("script", config.get("script"));
            result.set("config", config.get("config"));

            return result;
        }
    }

    // Custom: input.java { class = "...", config = {...} }
    static class JavaInputFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable config = args.checktable(1);

            LuaTable result = new LuaTable();
            result.set("type", LuaValue.valueOf("java"));
            result.set("class", config.get("class"));
            result.set("config", config.get("config"));

            return result;
        }
    }
}