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
        return module;
    }

    static class EchoFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            return args.checktable(1);
        }
    }
}