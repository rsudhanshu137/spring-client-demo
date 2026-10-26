# MCP Client Demo

A Spring Boot application demonstrating how to build an **AI chat assistant backed by the Model Context Protocol (MCP)**. The app uses [Spring AI](https://spring.io/projects/spring-ai) to connect an OpenAI-powered `ChatClient` to one or more MCP servers, so the LLM can call external tools (filesystem access, remote APIs, custom Java-based MCP servers, etc.) while answering user questions through a simple web chat UI.

## What is MCP?

The [Model Context Protocol](https://modelcontextprotocol.io/) is an open standard that lets LLMs call external "tools" (functions, APIs, data sources) in a provider-agnostic way. This project acts as an **MCP client**: it spins up one or more MCP servers as child processes (via stdio), discovers the tools they expose, and makes those tools available to the OpenAI chat model through Spring AI's tool-calling support.

## How it works

1. `McpClientDemoApplication` boots a standard Spring Boot app.
2. On startup, Spring AI's MCP client auto-configuration reads `mcp-server.json` and launches the configured MCP servers as subprocesses.
3. `MCPClientController` builds a `ChatClient` wired with `ToolCallbackProvider`, which exposes every tool discovered from the MCP servers to the chat model.
4. `HomeController` serves a Thymeleaf-based chat page (`chat.html`).
5. The browser sends user messages to `GET /mcp-client/send?message=...`, the LLM decides whether it needs to call an MCP tool to answer, and the final response is returned to the UI.

```
Browser (chat.html) --> /mcp-client/send --> ChatClient (OpenAI) --> MCP tool calls --> MCP servers (stdio)
```

## Tech stack

- Java 21
- Spring Boot 4.0.3
- Spring AI 2.0.0 (`spring-ai-starter-mcp-client`, `spring-ai-starter-model-openai`)
- Thymeleaf (server-rendered chat UI)
- springdoc-openapi (Swagger UI)
- Maven

## Project structure

```
src/main/java/com/javatechie/
├── McpClientDemoApplication.java      # Spring Boot entry point
└── controller/
    ├── HomeController.java            # Serves the chat page
    └── MCPClientController.java       # /mcp-client/send endpoint, wires ChatClient + MCP tools

src/main/resources/
├── application.yaml                   # OpenAI + MCP client configuration
├── mcp-server.json.example            # Template for MCP server definitions (copy -> mcp-server.json)
└── templates/chat.html                # Chat UI
```

## Getting started

### Prerequisites

- Java 21+
- An [OpenAI API key](https://platform.openai.com/api-keys)
- Node.js (only if you use the example `npx`-based MCP servers below)

### 1. Configure your MCP servers

Copy the example config and fill in your own paths/keys. **This file is git-ignored** so secrets never get committed.

```bash
cp src/main/resources/mcp-server.json.example src/main/resources/mcp-server.json
```

Edit `mcp-server.json` to point at the MCP servers you want the assistant to use, for example:

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/allowed/dir"]
    }
  }
}
```

Each entry can be any MCP server reachable via stdio — an `npx`-based package, a remote MCP proxy (`mcp-remote`), or your own Java/JAR-based MCP server.

### 2. Set your OpenAI API key

```bash
export OPENAI_API_KEY=sk-...
```

### 3. Run the app

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.

### 4. Chat

Open `http://localhost:8080` in your browser and start chatting. The assistant will transparently call any tools exposed by your configured MCP servers when needed.

You can also call the API directly:

```bash
curl "http://localhost:8080/mcp-client/send?message=What%20files%20are%20in%20my%20Downloads%20folder%3F"
```

## Configuration reference

`src/main/resources/application.yaml`:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
    mcp:
      client:
        enabled: true
        stdio:
          servers-configuration: 'classpath:mcp-server.json'
```

## Security notes

- `mcp-server.json` is excluded via `.gitignore` because it typically holds API keys and local file paths. Always keep real credentials out of version control — use `mcp-server.json.example` as the shareable template.
- Never commit `OPENAI_API_KEY` or similar secrets; pass them as environment variables.
