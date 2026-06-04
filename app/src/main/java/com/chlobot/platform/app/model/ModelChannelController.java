package com.chlobot.platform.app.model;

import com.chlobot.platform.agent.model.springai.ModelChannelRouter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/model/channels")
public class ModelChannelController {

    private final ModelChannelRouter modelChannelRouter;

    public ModelChannelController(ModelChannelRouter modelChannelRouter) {
        this.modelChannelRouter = modelChannelRouter;
    }

    @Operation(summary = "Get Spring AI model channel routing health")
    @GetMapping
    public Map<String, Object> channels() {
        return modelChannelRouter.health();
    }
}
