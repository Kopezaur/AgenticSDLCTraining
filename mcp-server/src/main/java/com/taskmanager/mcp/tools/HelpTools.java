package com.taskmanager.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class HelpTools {

    @Tool(name = "mcp-help", description = "Returns a description of all available MCP tools for the Task Manager.")
    public String getHelp() {
        return """
                Available MCP Tools for Task Manager:

                1. mcp-schema-tasks
                   - Description: Returns the database schema for the tasks table as a simplified JSON Schema.
                   - Parameters: None
                   - Use this first to understand the data structure before inserting tasks.

                2. mcp-tasks
                   - Description: Inserts a batch of tasks into the database.
                   - Parameters:
                     - tasks (required): A list of task objects, each with:
                       - title (string, required, max 100 chars)
                       - description (string, optional, max 500 chars)
                       - status (string, one of: TODO, IN_PROGRESS, DONE; defaults to TODO)
                       - dueDate (string, optional, format: yyyy-MM-dd)
                   - Returns: JSON with inserted and failed counts.

                3. mcp-tasks-summary
                   - Description: Returns summary statistics of all tasks in the database.
                   - Parameters: None
                   - Returns: JSON with totalTasks and breakdown by status.

                4. mcp-help
                   - Description: Returns this help text describing all available tools.
                   - Parameters: None

                Typical workflow:
                  1. Call mcp-schema-tasks to inspect the schema
                  2. Call mcp-tasks to insert task records
                  3. Call mcp-tasks-summary to verify the insertion
                """;
    }
}
