package io.github.fpedrazav02.panela.runner.output;

import io.github.fpedrazav02.panela.model.Output;

public class LuaOutputRunner implements OutputRunner {

    @Override
    public void execute(Output output, Object inputData) throws Exception {
        throw new UnsupportedOperationException("Lua outputs not implemented yet");
    }

    @Override
    public String getType() {
        return "lua";
    }
}