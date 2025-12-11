package com.example.mcp.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mcp.tools.SplunkTools;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/mcp/tools/splunk")
public class SplunkToolController {

    private final SplunkTools tools;

    public SplunkToolController(SplunkTools tools) {
        this.tools = tools;
    }

    // ✔ Call SplunkTools.list()
    @GetMapping("/indexes")
    public Mono<String> listIndexes() {
        return tools.list();
    }

     //✔ Call SplunkTools.search(String query)
    @GetMapping("/search")
    public Mono<String> createSearch(@RequestParam String query) {
        return tools.search(query);
    }

    // ✔ Call SplunkTools.results(String sid)
    @GetMapping("/results")
    public Mono<String> getResults(@RequestParam String sid) {
        return tools.results(sid);
    }

    // ✔ Call SplunkTools.send(String index, String event)
    @PostMapping("/event")
    public Mono<String> sendEvent(@RequestBody Map<String, String> body) {
        return tools.send(body.get("index"), body.get("event"));
    }
}
