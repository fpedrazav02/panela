package io.github.fpedrazav02.panela.parser.modules;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class OutputModule extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable module = new LuaTable();

        // Built-in
        module.set("file", new FileFunction()); // ✅ FALTABA ESTO

        // Custom
        module.set("lua", new LuaOutputFunction());
        module.set("java", new JavaOutputFunction());

        return module;
    }

    // Built-in: output.file
    static class FileFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            LuaTable config = args.checktable(1);

            LuaTable result = new LuaTable();
            result.set("type", LuaValue.valueOf("file"));
            result.set("from", config.get("from"));
            result.set("config", config);

            return result;
        }
    }

    // Custom: output.lua
    static class LuaOutputFunction extends VarArgFunction {
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

    // Custom: output.java
    static class JavaOutputFunction extends VarArgFunction {
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