package io.github.fpedrazav02.panela.model.types;

import java.util.Locale;

public enum FileType {
    JSON("json"),
    TXT("txt"),
    CSV("csv");

    private final String ext;

    FileType(String ext) {
        this.ext = ext;
    }

    public String ext() {
        return ext;
    }

    public static FileType from(Object v) {
        if (v == null) throw new IllegalArgumentException("Missing file type");
        String s = v.toString().trim().toLowerCase();
        return switch (s) {
            case "csv", "text/csv" -> CSV;
            case "json", "application/json" -> JSON;
            case "txt", "text", "text/plain" -> TXT;
            default -> throw new IllegalArgumentException("Unknown file type: " + v);
        };
    }
}