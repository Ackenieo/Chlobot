package com.chlobot.platform.agent.model;

import reactor.core.publisher.Flux;

public interface ModelClient {

    ModelResponse chat(ModelRequest request);

    Flux<ModelResponse> stream(ModelRequest request);
}
