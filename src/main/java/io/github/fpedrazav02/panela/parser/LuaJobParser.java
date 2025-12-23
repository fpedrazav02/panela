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
            String script = inputData.get("script").isnil() ? null : inputData.get("script").tojstring();
            String className = inputData.get("class").isnil() ? null : inputData.get("class").tojstring();

            Map<String, Object> config = new HashMap<>();
            LuaValue configValue = inputData.get("config");
            if (!configValue.isnil() && configValue.istable()) {
                config = parseTable(configValue.checktable());
            }

            inputs.add(new Input(name, type, script, className, config));
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

            String type = transformData.get("type").tojstring();
            String from = transformData.get("from").isnil() ? null : transformData.get("from").tojstring();
            String script = transformData.get("script").isnil() ? null : transformData.get("script").tojstring();
            String className = transformData.get("class").isnil() ? null : transformData.get("class").tojstring();

            Map<String, Object> config = new HashMap<>();
            LuaValue configValue = transformData.get("config");
            if (!configValue.isnil() && configValue.istable()) {
                config = parseTable(configValue.checktable());
            }

            transforms.add(new Transform(name, type, from, script, className, config));
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

            String type = outputData.get("type").tojstring();
            String from = outputData.get("from").isnil() ? null : outputData.get("from").tojstring();
            String script = outputData.get("script").isnil() ? null : outputData.get("script").tojstring();
            String className = outputData.get("class").isnil() ? null : outputData.get("class").tojstring();

            Map<String, Object> config = new HashMap<>();
            LuaValue configValue = outputData.get("config");
            if (!configValue.isnil() && configValue.istable()) {
                config = parseTable(configValue.checktable());
            }

            outputs.add(new Output(name, type, from, script, className, config));
        }

        return outputs;
    }

    private Map<String, Object> parseTable(LuaTable table) {
        Map<String, Object> map = new HashMap<>();
        LuaValue key = LuaValue.NIL;

        while (true) {
            Varargs next = table.next(key);
            if ((key = next.arg1()).isnil()) break;

            String k = key.tojstring();
            LuaValue v = next.arg(2);

            if (v.isstring()) {
                map.put(k, v.tojstring());
            } else if (v.isint()) {
                map.put(k, v.toint());
            } else if (v.isnumber()) {
                map.put(k, v.todouble());
            } else if (v.isboolean()) {
                map.put(k, v.toboolean());
            } else if (v.istable()) {
                map.put(k, parseTable(v.checktable()));
            }
        }

        return map;
    }
}