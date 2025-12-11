package com.example.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class SplunkTools {
	private final SplunkService splunk;

	public SplunkTools(SplunkService splunk) {
		this.splunk = splunk;
	}

	@Tool(name = "splunk_list_indexes", description = "Return Splunk indexes")
    public Mono<String> list() {
        return splunk.listIndexes();
    }


	@Tool(name = "splunk_search", description = "Create search job")
    public Mono<String> search(String query) {
        return splunk.createSearchJob(query);
    }

	@Tool(name = "splunk_get_search_results", description = "Get results for SID")
	public Mono<String> results(String sid) {
		return splunk.getSearchResults(sid);
	}

	@Tool(name = "splunk_send_event", description = "Send event to HEC")
	public Mono<String> send(String index, String event) {
		return splunk.sendEvent(index, event);
	}
}