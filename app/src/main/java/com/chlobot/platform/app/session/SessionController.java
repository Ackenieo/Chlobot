package com.chlobot.platform.app.session;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Operation(summary = "Create an Agent session")
    @PostMapping
    public AgentSession create(@RequestBody CreateSessionRequest request) {
        return sessionService.create(request);
    }

    @Operation(summary = "List Agent sessions")
    @GetMapping
    public List<AgentSession> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return sessionService.list(page, size);
    }

    @Operation(summary = "Get an Agent session")
    @GetMapping("/{sessionId}")
    public AgentSession get(@PathVariable String sessionId) {
        return sessionService.get(sessionId);
    }

    @Operation(summary = "Delete an Agent session")
    @DeleteMapping("/{sessionId}")
    public Map<String, Object> delete(@PathVariable String sessionId) {
        sessionService.delete(sessionId);
        return Map.of("sessionId", sessionId, "deleted", true);
    }

    @Operation(summary = "Add a message to a session")
    @PostMapping("/{sessionId}/messages")
    public ConversationMessage addMessage(@PathVariable String sessionId, @Valid @RequestBody CreateMessageRequest request) {
        return sessionService.addMessage(sessionId, request);
    }

    @Operation(summary = "List session messages")
    @GetMapping("/{sessionId}/messages")
    public List<ConversationMessage> listMessages(@PathVariable String sessionId) {
        return sessionService.listMessages(sessionId);
    }
}
