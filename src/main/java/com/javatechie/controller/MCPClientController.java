package com.javatechie.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcp-client")
public class MCPClientController {

    private final ChatClient chatClient;

    public MCPClientController(ChatClient.Builder chatClientBuilder, ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }

    @GetMapping("/send")
    public String sendMessageToMCPServer(@RequestParam String message){
                String response=chatClient.prompt(message)
                        .call()
                        .content();
                return "Response from MCP :" + response;
    }

}
