package com.example.motion.api;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.interfaces.IMotionLayer;
import com.example.motion.api.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/layers")
public class LayerController {

    private final ICharacterMotionService motionService;

    public LayerController(ICharacterMotionService motionService) {
        this.motionService = motionService;
    }

    @GetMapping
    public ResponseEntity<List<LayerResponse>> getActiveLayers() {
        List<IMotionLayer> layers = motionService.getActiveLayers();
        List<LayerResponse> response = layers.stream()
            .map(layer -> new LayerResponse(
                layer.getClass().getSimpleName(),
                layer.getClass().getName()
            ))
            .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<LayerResponse> addLayer(@RequestBody LayerRequest request) {
        try {
            Class<?> layerClass = Class.forName(request.getClassName());
            IMotionLayer layer = (IMotionLayer) layerClass.getDeclaredConstructor().newInstance();
            
            boolean added = motionService.addMotionLayer(layer, request.getPriority());
            if (!added) {
                return ResponseEntity.badRequest()
                    .body(new LayerResponse("Layer already exists", request.getClassName()));
            }
            
            return ResponseEntity.ok(new LayerResponse(
                layer.getClass().getSimpleName(),
                request.getClassName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new LayerResponse("Error creating layer", request.getClassName()));
        }
    }

    @DeleteMapping("/{className}")
    public ResponseEntity<Void> removeLayer(@PathVariable String className) {
        try {
            Class<?> layerClass = Class.forName(className);
            IMotionLayer layer = (IMotionLayer) layerClass.getDeclaredConstructor().newInstance();
            
            boolean removed = motionService.removeMotionLayer(layer);
            if (!removed) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{className}/priority")
    public ResponseEntity<LayerResponse> updateLayerPriority(
            @PathVariable String className,
            @RequestBody PriorityRequest request) {
        try {
            Class<?> layerClass = Class.forName(className);
            IMotionLayer layer = (IMotionLayer) layerClass.getDeclaredConstructor().newInstance();
            
            boolean updated = motionService.updateLayerPriority(layer, request.getPriority());
            if (!updated) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(new LayerResponse(
                layer.getClass().getSimpleName(),
                className
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("Error processing request", e.getMessage()));
    }
}