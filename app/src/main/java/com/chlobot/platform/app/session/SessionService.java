package com.chlobot.platform.app.session;

import com.chlobot.platform.common.error.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;

    public SessionService(SessionRepository sessionRepository, MessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    public AgentSession create(CreateSessionRequest request) {
        Instant now = Instant.now();
        String title = request.title() == null || request.title().isBlank() ? "New Agent Session" : request.title();
        return sessionRepository.create(new AgentSession(UUID.randomUUID().toString(), title,
                request.metadata() == null ? Map.of() : request.metadata(), now, now));
    }

    public List<AgentSession> list(int page, int size) {
        int normalizedSize = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * normalizedSize;
        return sessionRepository.list(normalizedSize, offset);
    }

    public AgentSession get(String id) {
        return sessionRepository.findById(id).orElseThrow(() -> new NotFoundException("Session not found: " + id));
    }

    public ConversationMessage addMessage(String sessionId, CreateMessageRequest request) {
        get(sessionId);
        return messageRepository.create(new ConversationMessage(null, sessionId, request.role(), request.content(),
                request.metadata() == null ? Map.of() : request.metadata(), Instant.now()));
    }

    public List<ConversationMessage> listMessages(String sessionId) {
        get(sessionId);
        return messageRepository.listBySession(sessionId);
    }

    public void delete(String id) {
        get(id);
        messageRepository.deleteBySession(id);
        sessionRepository.delete(id);
    }
}
