package io.github.fpedrazav02.panela.model.tabular;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TableTest {

    @Test
    public void storesColumnsAndRows() {
        Row row = new Row(Map.of("name", "Alice", "age", "30"));
        Table table = new Table(List.of("name", "age"), List.of(row));

        assertEquals(2, table.colCount());
        assertEquals(1, table.rowCount());
        assertTrue(table.hasColumn("name"));
        assertFalse(table.hasColumn("salary"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDuplicateColumn() {
        new Table(List.of("id", "id"), List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankColumn() {
        new Table(List.of("id", "  "), List.of());
    }

    @Test
    public void emptyTableIsValid() {
        Table table = new Table(List.of(), List.of());
        assertEquals(0, table.rowCount());
        assertEquals(0, table.colCount());
    }
}
