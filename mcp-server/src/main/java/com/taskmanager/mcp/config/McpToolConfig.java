package com.taskmanager.mcp.config;

import com.taskmanager.mcp.tools.HelpTools;
import com.taskmanager.mcp.tools.SchemaTools;
import com.taskmanager.mcp.tools.TaskTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider taskManagerTools(
            SchemaTools schemaTools,
            TaskTools taskTools,
            HelpTools helpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(schemaTools, taskTools, helpTools)
                .build();
    }
}
