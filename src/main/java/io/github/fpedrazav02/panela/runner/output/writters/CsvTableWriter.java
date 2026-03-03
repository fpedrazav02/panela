package io.github.fpedrazav02.panela.runner.output.writters;

import io.github.fpedrazav02.panela.model.tabular.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CsvTableWriter {

    private CsvTableWriter() {}

    public static void write(Table table, Path outPath) throws Exception {
        Files.createDirectories(outPath.getParent());

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(table.columns().toArray(String[]::new))
                .build();

        try (BufferedWriter writer = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (var row : table.rows()) {
                Object[] values = table.columns().stream()
                        .map(row::get)
                        .toArray(Object[]::new);
                printer.printRecord(values);
            }
            printer.flush();
        }
    }
}