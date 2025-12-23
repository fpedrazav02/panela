package io.github.fpedrazav02.panela.parser;

import io.github.fpedrazav02.panela.parser.modules.InputModule;
import io.github.fpedrazav02.panela.parser.modules.JobModule;
import io.github.fpedrazav02.panela.parser.modules.OutputModule;
import io.github.fpedrazav02.panela.parser.modules.TransformModule;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.nio.file.Path;

public class LuaJobParser {

    private static class LuaJobParserHolder {
        private static final LuaJobParser uniqueInstance = new LuaJobParser();
    }

    public static LuaJobParser getInstance() {
        return LuaJobParserHolder.uniqueInstance;
    }

    public void parse(Path jobLuaPath) throws Exception {
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
        LuaTable inputs = jobTable.get("inputs").checktable();
        LuaTable transforms = jobTable.get("transforms").checktable();
        LuaTable outputs = jobTable.get("outputs").checktable();

        // Print summary
        System.out.println("📦 Job: " + name + " v" + version);
        System.out.println("📥 Inputs: " + countKeys(inputs));
        System.out.println("🔄 Transforms: " + countKeys(transforms));
        System.out.println("📤 Outputs: " + countKeys(outputs));
    }

    private int countKeys(LuaTable table) {
        int count = 0;
        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = table.next(key);
            if ((key = next.arg1()).isnil()) break;
            count++;
        }
        return count;
    }
}