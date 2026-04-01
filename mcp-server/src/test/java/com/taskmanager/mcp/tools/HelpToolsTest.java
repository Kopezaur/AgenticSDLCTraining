package com.taskmanager.mcp.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpToolsTest {

    private final HelpTools helpTools = new HelpTools();

    @Test
    void getHelp_containsAllToolNames() {
        String help = helpTools.getHelp();

        assertTrue(help.contains("mcp-schema-tasks"));
        assertTrue(help.contains("mcp-tasks"));
        assertTrue(help.contains("mcp-tasks-summary"));
        assertTrue(help.contains("mcp-help"));
    }

    @Test
    void getHelp_containsWorkflow() {
        String help = helpTools.getHelp();

        assertTrue(help.contains("Typical workflow"));
    }
}
