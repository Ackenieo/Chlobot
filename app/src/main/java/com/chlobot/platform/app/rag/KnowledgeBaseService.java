package com.chlobot.platform.app.rag;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository repository;

    public KnowledgeBaseService(KnowledgeBaseRepository repository) {
        this.repository = repository;
    }

    public List<KnowledgeBase> findAll() {
        return repository.findAll();
    }

    public KnowledgeBase create(CreateKnowledgeBaseRequest request) {
        return repository.create(request);
    }

    public KnowledgeBase update(String id, UpdateKnowledgeBaseRequest request) {
        return repository.update(id, request);
    }

    public void delete(String id) {
        repository.delete(id);
    }

    public KnowledgeBase get(String id) {
        return repository.get(id);
    }
}
