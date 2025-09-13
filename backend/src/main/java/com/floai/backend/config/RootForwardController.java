package com.floai.backend.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootForwardController {

    // If you KEEP context-path=/api, this maps to http://localhost:8081/api/
    // If you REMOVE context-path, it maps to http://localhost:8081/
    @GetMapping({"/", "/index", "/home"})
    public String forwardToIndex() {
        return "forward:/index.html"; // served from src/main/resources/static/index.html
    }
}
