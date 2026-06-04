package com.chlobot.platform.app.model;

import com.chlobot.platform.app.rag.RagSearchRequest;
import com.chlobot.platform.app.rag.RagSearchResult;
import com.chlobot.platform.app.rag.RagService;
import com.chlobot.platform.agent.model.ModelClient;
import com.chlobot.platform.agent.model.ModelRequest;
import com.chlobot.platform.agent.model.springai.ChatIntent;
import com.chlobot.platform.agent.model.springai.StructuredOutput;
import com.chlobot.platform.agent.model.springai.StructuredOutputHelper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ModelClient modelClient;
    private final StructuredOutputHelper structuredOutputHelper;
    private final RagService ragService;

    public ChatController(ModelClient modelClient, StructuredOutputHelper structuredOutputHelper, RagService ragService) {
        this.modelClient = modelClient;
        this.structuredOutputHelper = structuredOutputHelper;
        this.ragService = ragService;
    }

    @Operation(summary = "Chat through the Spring AI model abstraction")
    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        com.chlobot.platform.agent.model.ModelResponse response = modelClient.chat(toModelRequest(request));
        return new ChatResponse(response.content(), response.provider(), response.model(), response.mock());
    }

    @Operation(summary = "Stream chat through the Spring AI model abstraction")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> stream(@Valid @RequestBody ChatRequest request) {
        return modelClient.stream(toModelRequest(request))
                .map(response -> new ChatResponse(response.content(), response.provider(), response.model(), response.mock()));
    }

    @Operation(summary = "Parse a Spring AI response as validated structured output")
    @PostMapping("/structured-intent")
    public ChatResponse structuredIntent(@Valid @RequestBody ChatRequest request) {
        com.chlobot.platform.agent.model.ModelResponse response = modelClient.chat(toStructuredModelRequest(request));
        if (response.mock()) {
            ChatIntentResponse intent = new ChatIntentResponse("mock-chat", request.message());
            return new ChatResponse(response.content(), response.provider(), response.model(), true, intent);
        }
        StructuredOutput<ChatIntent> output = structuredOutputHelper.parse("chat-intent", response.content(), ChatIntent.class);
        ChatIntent intent = output.data();
        return new ChatResponse(response.content(), response.provider(), response.model(), response.mock(),
                new ChatIntentResponse(intent.intent(), intent.summary()));
    }

    private ModelRequest toModelRequest(ChatRequest request) {
        List<Message> messages = new ArrayList<>();
        String knowledgeContext = knowledgeContext(request);
        if (request.system() != null && !request.system().isBlank()) {
            messages.add(new SystemMessage(request.system() + knowledgeContext));
        } else if (!knowledgeContext.isBlank()) {
            messages.add(new SystemMessage(knowledgeContext));
        }
        messages.add(new UserMessage(request.message()));
        return new ModelRequest(new Prompt(messages), null, Map.of("endpoint", "chat"));
    }

    private String knowledgeContext(ChatRequest request) {
        if (request.knowledgeBaseIds() == null || request.knowledgeBaseIds().isEmpty()) {
            return "";
        }
        List<RagSearchResult> results = request.knowledgeBaseIds().stream()
                .filter(id -> id != null && !id.isBlank())
                .flatMap(id -> ragService.search(new RagSearchRequest(request.message(), 3, "semantic", id)).stream())
                .limit(8)
                .toList();
        if (results.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("\n\nUse the following selected knowledge base context when helpful. If it is irrelevant, answer normally.\n");
        for (int index = 0; index < results.size(); index++) {
            RagSearchResult result = results.get(index);
            builder.append("[KB-").append(index + 1).append("] ")
                    .append(result.title()).append("\n")
                    .append(result.content()).append("\n");
        }
        return builder.toString();
    }

    private ModelRequest toStructuredModelRequest(ChatRequest request) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("Return only JSON matching this Java record: {\"intent\":\"short intent\",\"summary\":\"one sentence summary\"}."));
        messages.add(new UserMessage(request.message()));
        return new ModelRequest(new Prompt(messages), null, Map.of("endpoint", "chat-structured-intent"));
    }
}
