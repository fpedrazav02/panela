package io.github.fpedrazav02.panela.runner.input;

import io.github.fpedrazav02.panela.model.Input;

public class LuaInputRunner implements InputRunner {

    @Override
    public Object execute(Input input) throws Exception {
        // TODO: Implement load from LUA script
        throw new UnsupportedOperationException("Lua inputs not implemented yet");
    }

    @Override
    public String getType() {
        return "lua";
    }
}