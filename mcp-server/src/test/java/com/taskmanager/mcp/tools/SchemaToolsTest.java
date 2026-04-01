package com.taskmanager.mcp.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaToolsTest {

    private final SchemaTools schemaTools = new SchemaTools();

    @Test
    void getTaskSchema_containsAllColumns() {
        String schema = schemaTools.getTaskSchema();

        assertTrue(schema.contains("\"table\": \"tasks\""));
        assertTrue(schema.contains("\"id\""));
        assertTrue(schema.contains("\"title\""));
        assertTrue(schema.contains("\"description\""));
        assertTrue(schema.contains("\"status\""));
        assertTrue(schema.contains("\"dueDate\""));
    }

    @Test
    void getTaskSchema_containsStatusEnum() {
        String schema = schemaTools.getTaskSchema();

        assertTrue(schema.contains("TODO"));
        assertTrue(schema.contains("IN_PROGRESS"));
        assertTrue(schema.contains("DONE"));
    }
}
