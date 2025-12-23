package io.github.fpedrazav02.panela.parser;

import io.github.fpedrazav02.panela.model.*;
import io.github.fpedrazav02.panela.parser.modules.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.nio.file.Path;
import java.util.*;

public class LuaJobParser {

    private static class LuaJobParserHolder {
        private static final LuaJobParser uniqueInstance = new LuaJobParser();
    }

    public static LuaJobParser getInstance() {
        return LuaJobParserHolder.uniqueInstance;
    }

    public Job parse(Path jobLuaPath) throws Exception {
        Globals globals = JsePlatform.standardGlobals();

        // Register modules
        globals.get("package").get("preload").set("job", new JobModule());
        globals.get("package").get("preload").set("input", new InputModule());
        globals.get("package").get("preload").set("output", new OutputModule());
        globals.get("package").get("preload").set("transform", new TransformModule());

        // Execute job.lua
        LuaValue chunk = globals.loadfile(jobLuaPath.toString());
        LuaValue result = chunk.call();

        // Extract job metadata
        LuaTable jobTable = result.checktable();

        String name = jobTable.get("name").tojstring();
        String version = jobTable.get("version").tojstring();
        List<Input> inputs = parseInputs(jobTable.get("inputs").checktable());
        List<Transform> transforms = parseTransforms(jobTable.get("transforms").checktable());
        List<Output> outputs = parseOutputs(jobTable.get("outputs").checktable());

        return new Job(name, version, inputs, transforms, outputs);
    }

    private List<Input> parseInputs(LuaTable inputsTable) {
        List<Input> inputs = new ArrayList<>();
        LuaValue key = LuaValue.NIL;

        while (true) {
            Varargs next = inputsTable.next(key);
            if ((key = next.arg1()).isnil()) break;

            String name = key.tojstring();
            LuaTable inputData = next.arg(2).checktable();

            String type = inputData.get("type").tojstring();
            String data = inputData.get("data").tojstring();

            inputs.add(new Input(name, type, data));
        }

        return inputs;
    }

    private List<Transform> parseTransforms(LuaTable transformsTable) {
        List<Transform> transforms = new ArrayList<>();
        LuaValue key = LuaValue.NIL;

        while (true) {
            Varargs next = transformsTable.next(key);
            if ((key = next.arg1()).isnil()) break;

            String name = key.tojstring();
            LuaTable transformData = next.arg(2).checktable();

            String function = transformData.get("function").tojstring();
            String from = transformData.get("from").tojstring();

            Map<String, String> params = new HashMap<>();

            LuaValue paramsValue = transformData.get("params");
            if (!paramsValue.isnil() && paramsValue.istable()) {
                LuaValue paramsKey = LuaValue.NIL;
                LuaTable paramsTable = paramsValue.checktable();

                while (true) {
                    Varargs paramsNext = paramsTable.next(paramsKey);
                    if ((paramsKey = paramsNext.arg1()).isnil()) break;
                    params.put(paramsKey.tojstring(), paramsNext.arg(2).tojstring());
                }
            }

            transforms.add(new Transform(name, function, from, params));
        }

        return transforms;
    }

    private List<Output> parseOutputs(LuaTable outputsTable) {
        List<Output> outputs = new ArrayList<>();
        LuaValue key = LuaValue.NIL;

        while (true) {
            Varargs next = outputsTable.next(key);
            if ((key = next.arg1()).isnil()) break;

            String name = key.tojstring();
            LuaTable outputData = next.arg(2).checktable();

            String destination = outputData.get("destination").tojstring();
            String from = outputData.get("from").tojstring();

            outputs.add(new Output(name, destination, from));
        }

        return outputs;
    }
}