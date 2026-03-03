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

    public static FileType from(Object o) {
        if (o == null) throw new IllegalArgumentException("type is required");
        String s = o.toString().trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "json" -> JSON;
            case "txt"  -> TXT;
            case "csv"  -> CSV;
            default -> throw new IllegalArgumentException("Unsupported file type: " + o);
        };
    }
}