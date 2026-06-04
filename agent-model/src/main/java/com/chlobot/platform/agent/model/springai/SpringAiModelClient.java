package com.chlobot.platform.agent.model.springai;

import com.chlobot.platform.agent.model.ModelClient;
import com.chlobot.platform.agent.model.ModelRequest;
import com.chlobot.platform.agent.model.ModelResponse;
import reactor.core.publisher.Flux;

public class SpringAiModelClient implements ModelClient {

    private final ModelChannelRouter modelChannelRouter;

    public SpringAiModelClient(ModelChannelRouter modelChannelRouter) {
        this.modelChannelRouter = modelChannelRouter;
    }

    @Override
    public ModelResponse chat(ModelRequest request) {
        return modelChannelRouter.chat(request);
    }

    @Override
    public Flux<ModelResponse> stream(ModelRequest request) {
        return modelChannelRouter.stream(request);
    }
}
