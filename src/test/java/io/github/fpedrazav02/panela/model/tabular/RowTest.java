package io.github.fpedrazav02.panela.model.tabular;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RowTest {

    @Test
    public void getReturnsValue() {
        Row row = new Row(Map.of("city", "Madrid"));
        assertEquals("Madrid", row.get("city"));
        assertNull(row.get("country"));
    }

    @Test
    public void withReturnsNewRowAndOriginalIsUnchanged() {
        Row original = new Row(new LinkedHashMap<>(Map.of("a", "1")));
        Row updated  = original.with("b", "2");

        assertNull(original.get("b"));
        assertEquals("2", updated.get("b"));
        assertEquals("1", updated.get("a"));
    }

    @Test
    public void dropRemovesColumn() {
        Row row     = new Row(new LinkedHashMap<>(Map.of("x", "1", "y", "2")));
        Row dropped = row.drop("x");

        assertNull(dropped.get("x"));
        assertEquals("2", dropped.get("y"));
    }

    @Test
    public void dropMissingColumnReturnsSameRow() {
        Row row = new Row(Map.of("a", "1"));
        assertSame(row, row.drop("z"));
    }
}
