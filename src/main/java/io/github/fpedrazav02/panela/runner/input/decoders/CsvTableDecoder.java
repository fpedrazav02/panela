package io.github.fpedrazav02.panela.runner.input.decoders;

import io.github.fpedrazav02.panela.model.tabular.Row;
import io.github.fpedrazav02.panela.model.tabular.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class CsvTableDecoder {

    public Table decode(Path csvPath) throws Exception {
        CSVFormat format = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(false)
                .build();

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = CSVParser.parse(reader, format)) {

            Map<String, Integer> headerMap = parser.getHeaderMap();
            if (headerMap == null || headerMap.isEmpty()) {
                throw new IllegalArgumentException("CSV has no header row: " + csvPath);
            }

            List<String> columns = headerMap.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .toList();

            List<Row> rows = new ArrayList<>();
            parser.forEach(rec -> {
                var m = new LinkedHashMap<String, String>();
                for (String c : columns) {
                    String v = rec.isMapped(c) ? rec.get(c) : null;
                    m.put(c, v);
                }
                rows.add(new Row(m));
            });

            return new Table(columns, rows);
        }
    }
}