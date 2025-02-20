package com.example.motion.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MotionController {

    @GetMapping("/animated-surface")
    public String getAnimatedSurface() {
        // Placeholder for the animated surface logic
        return "Animated surface showing movement, motion layer switching, and coordinates on a grid.";
    }
}
