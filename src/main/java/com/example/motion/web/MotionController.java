package com.example.motion.web;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.behavior.AdvancedWalkingLayer;
import com.example.motion.sys.behavior.BasicWalkingLayer;
import com.example.motion.sys.model.Direction;
import com.example.motion.sys.model.Vector3D;
import com.example.motion.sys.model.MotionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class MotionController {

    private final ICharacterMotionService motionService;

    @Autowired
    public MotionController(ICharacterMotionService motionService) {
        this.motionService = motionService;
    }

    @GetMapping("/")
    public String getIndex() {
        return "redirect:/index.html";
    }

    @GetMapping("/game")
    public String getGame() {
        return "redirect:/index.html";
    }

    @GetMapping("/animated-surface")
    @ResponseBody
    public String getAnimatedSurface() {
        return "Animated surface showing movement, motion layer switching, and coordinates on a grid. Access the motion behavior demo via /demo-motion endpoint.";
    }

    @GetMapping("/demo-motion")
    @ResponseBody
    public String demoMotionBehavior() {
        try {
            UUID characterId = UUID.randomUUID();
            BasicWalkingLayer basicLayer = new BasicWalkingLayer();
            AdvancedWalkingLayer advancedLayer = new AdvancedWalkingLayer();

            motionService.addMotionLayer(basicLayer, 1);
            motionService.registerMotionCallback(characterId, (id, state) -> {
                System.out.printf("Position: %s, Speed: %.2f%n",
                        state.getPosition(), state.getSpeed());
            });

            Direction walkDirection = new Direction(new Vector3D(1, 0, 0));
            motionService.setMovementDirection(characterId, walkDirection, 0.5f).join();
            Thread.sleep(2000);

            motionService.removeMotionLayer(basicLayer);
            motionService.addMotionLayer(advancedLayer, 1);
            Thread.sleep(500);

            advancedLayer.setGaitType(characterId, AdvancedWalkingLayer.GaitType.NORMAL);
            motionService.setMovementDirection(characterId, walkDirection, 0.5f).join();
            Thread.sleep(2000);

            advancedLayer.setGaitType(characterId, AdvancedWalkingLayer.GaitType.SNEAKING);
            motionService.setMovementDirection(characterId, walkDirection, 0.3f).join();
            Thread.sleep(2000);

            advancedLayer.setGaitType(characterId, AdvancedWalkingLayer.GaitType.LIMPING);
            motionService.setMovementDirection(characterId, walkDirection, 0.4f).join();
            Thread.sleep(2000);

            motionService.stopMotion(characterId).join();
            Thread.sleep(500);

            motionService.removeMotionLayer(advancedLayer);
            motionService.addMotionLayer(basicLayer, 1);
            motionService.setMovementDirection(characterId, walkDirection, 0.5f).join();
            Thread.sleep(2000);

            motionService.stopMotion(characterId).join();
            return "Motion behavior demo completed successfully!";
        } catch (Exception e) {
            return "Error during motion behavior demo: " + e.getMessage();
        }
    }

    @GetMapping("/current-position")
    @ResponseBody
    public String getCurrentPosition() {
        try {
            UUID characterId = UUID.randomUUID();
            MotionState currentState = motionService.getMotionState(characterId);
            return formatPositionInGrid(currentState);
        } catch (Exception e) {
            return "Error retrieving current position: " + e.getMessage();
        }
    }

    private String formatPositionInGrid(MotionState state) {
        StringBuilder grid = new StringBuilder();
        grid.append("Current Position:\n");
        grid.append(String.format("Position(x=%.2f, y=%.2f, z=%.2f), Speed: %.2f\n",
                state.getPosition().getX(),
                state.getPosition().getY(),
                state.getPosition().getZ(),
                state.getSpeed()));
        return grid.toString();
    }
}