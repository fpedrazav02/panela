package io.github.fpedrazav02.panela.model.types;

import io.github.fpedrazav02.panela.exceptions.custom.InvalidInputException;

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

    public static FileType from(Object v) throws InvalidInputException {
        if (v == null) throw new InvalidInputException("Missing file type — specify type = \"csv\", \"json\" or \"txt\"");
        String s = v.toString().trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "csv", "text/csv"             -> CSV;
            case "json", "application/json"    -> JSON;
            case "txt", "text", "text/plain"   -> TXT;
            default -> throw new InvalidInputException("Unknown file type '" + v + "' — expected csv, json or txt");
        };
    }
}