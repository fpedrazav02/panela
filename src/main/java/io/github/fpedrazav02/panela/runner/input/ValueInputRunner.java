package io.github.fpedrazav02.panela.runner.input;

import io.github.fpedrazav02.panela.model.Input;

public class ValueInputRunner implements InputRunner {

    @Override
    public Object execute(Input input) {
        Object data = input.config().get("data");
        String type = (String) input.config().get("type");

        return switch (type) {
            case "string" -> data.toString();
            case "int" -> Integer.parseInt(data.toString());
            case "double" -> Double.parseDouble(data.toString());
            case "boolean" -> Boolean.parseBoolean(data.toString());
            default -> data;
        };
    }

    @Override
    public String getType() {
        return "value";
    }
}