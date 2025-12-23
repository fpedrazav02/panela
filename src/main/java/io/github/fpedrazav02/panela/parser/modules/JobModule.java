package io.github.fpedrazav02.panela.parser.modules;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class JobModule extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable module = new LuaTable();
        module.set("define", new DefineFunction());
        return module;
    }

    static class DefineFunction extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            return args.checktable(1);
        }
    }
}