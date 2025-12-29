package com.example.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class SplunkTools {

	private static final Logger log = LoggerFactory.getLogger(SplunkTools.class);

	private final SplunkService splunk;

	public SplunkTools(SplunkService splunk) {
		this.splunk = splunk;
	}

	@Tool(name = "splunk_list_indexes", description = "Return Splunk indexes")
	public Mono<String> list() {
		log.info("MCP TOOL CALLED: splunk_list_indexes");
		return splunk.listIndexes();
	}

	@Tool(name = "run_splunk_query", description = "Execute a Splunk search query and return the results. "
			+ "This is the primary tool for running Splunk searches using SPL.")
	public Mono<String> runSplunkQuery(String query, Long waitSeconds) {
		log.info("MCP TOOL CALLED: run_splunk_query");
		return splunk.runSplunkQuery(query, waitSeconds != null ? waitSeconds : 2);
	}

	@Tool(name = "splunk_send_event", description = "Send event to HEC")
	public Mono<String> send(String index, String event) {
		log.info("MCP TOOL CALLED: splunk_send_event");
		return splunk.sendEvent(index, event);
	}
}
// package com.example.mcp.tools;

// import org.springframework.ai.tool.annotation.Tool;
// import org.springframework.stereotype.Component;

// import reactor.core.publisher.Mono;

// @Component
// public class SplunkTools {
// 	private final SplunkService splunk;

// 	public SplunkTools(SplunkService splunk) {
// 		this.splunk = splunk;
// 	}

// 	@Tool(name = "splunk_list_indexes", description = "Return Splunk indexes")
//     public Mono<String> list() {
//         return splunk.listIndexes();
//     }


// 	@Tool(name = "splunk_search", description = "Create search job")
//     public Mono<String> search(String query) {
//         return splunk.createSearchJob(query);
//     }

// 	@Tool(name = "splunk_get_search_results", description = "Get results for SID")
// 	public Mono<String> results(String sid) {
// 		return splunk.getSearchResults(sid);
// 	}

// 	@Tool(name = "splunk_send_event", description = "Send event to HEC")
// 	public Mono<String> send(String index, String event) {
// 		return splunk.sendEvent(index, event);
// 	}

// }
