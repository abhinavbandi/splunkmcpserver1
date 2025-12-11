package com.example.mcp;

import java.util.List;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.example.mcp.tools.SplunkTools;

@SpringBootApplication
public class SplunkMcpApplication {
	public static void main(String[] args) {
		SpringApplication.run(SplunkMcpApplication.class, args);
	}

	@Bean
	public List<ToolCallback> toolCallbacks(SplunkTools splunkTools) {
		return List.of(ToolCallbacks.from(splunkTools));
	}
}