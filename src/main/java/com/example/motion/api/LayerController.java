package com.example.motion.api;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.behavior.IMotionLayer;
import com.example.motion.sys.behavior.BasicWalkingLayer;
import com.example.motion.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/layers")
@Tag(name = "Layer Controller", description = "API zur Verwaltung von Motion Layers")
public class LayerController {

    private final ICharacterMotionService motionService;

    public LayerController(ICharacterMotionService motionService) {
        this.motionService = motionService;
    }

    @Operation(summary = "Liste aller aktiven Layer",
              description = "Gibt eine Liste aller aktuell aktiven Motion Layer zurück")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Layer erfolgreich abgerufen"),
        @ApiResponse(responseCode = "500", description = "Interner Serverfehler")
    })
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

    @Operation(summary = "Fügt einen neuen Layer hinzu",
              description = "Fügt einen neuen Motion Layer mit der angegebenen Priorität hinzu")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Layer erfolgreich hinzugefügt"),
        @ApiResponse(responseCode = "400", description = "Layer existiert bereits oder ungültige Parameter"),
        @ApiResponse(responseCode = "500", description = "Fehler beim Erstellen des Layers")
    })
    @PostMapping
    public ResponseEntity<LayerResponse> addLayer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Layer-Klasse und Priorität")
            @RequestBody LayerRequest request) {
        try {
            Class<?> layerClass = Class.forName(request.getClassName());
            IMotionLayer layer = (IMotionLayer) layerClass.getDeclaredConstructor().newInstance();
            
            boolean added = motionService.addMotionLayer(layer, request.getPriority());
            if (!added) {
                return ResponseEntity.status(400)
                    .body(new LayerResponse("Layer already exists", request.getClassName()));
            }
            
            return ResponseEntity.status(200).body(new LayerResponse(
                layer.getClass().getSimpleName(),
                request.getClassName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new LayerResponse("Error creating layer", request.getClassName()));
        }
    }

    @Operation(summary = "Entfernt einen Layer",
              description = "Entfernt einen Motion Layer aus dem System")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Layer erfolgreich entfernt"),
        @ApiResponse(responseCode = "404", description = "Layer nicht gefunden"),
        @ApiResponse(responseCode = "400", description = "Ungültige Layer-Klasse")
    })
    @DeleteMapping("/{className}")
    public ResponseEntity<Void> removeLayer(
            @Parameter(description = "Vollqualifizierter Klassenname des Layers")
            @PathVariable String className) {
        try {
            Class<?> layerClass = Class.forName(className);
            IMotionLayer layer = (IMotionLayer) layerClass.getDeclaredConstructor().newInstance();
            
            boolean removed = motionService.removeMotionLayer(layer);
            if (!removed) {
                return ResponseEntity.status(404).build();
            }
            
            return ResponseEntity.status(200).build();
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }

    @Operation(summary = "Aktualisiert die Layer-Priorität",
              description = "Ändert die Priorität eines existierenden Motion Layers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Priorität erfolgreich aktualisiert"),
        @ApiResponse(responseCode = "404", description = "Layer nicht gefunden"),
        @ApiResponse(responseCode = "400", description = "Ungültige Layer-Klasse oder Priorität")
    })
    @PutMapping("/{className}/priority")
    public ResponseEntity<LayerResponse> updateLayerPriority(
            @Parameter(description = "Vollqualifizierter Klassenname des Layers")
            @PathVariable String className,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Neue Priorität für den Layer")
            @RequestBody PriorityRequest request) {
        try {
            Class<?> layerClass = Class.forName(className);
            IMotionLayer layer = (IMotionLayer) layerClass.getDeclaredConstructor().newInstance();
            
            boolean updated = motionService.updateLayerPriority(layer, request.getPriority());
            if (!updated) {
                return ResponseEntity.status(404).build();
            }
            
            return ResponseEntity.status(200).body(new LayerResponse(
                layer.getClass().getSimpleName(),
                className
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(400)
            .body(new ErrorResponse("Error processing request", e.getMessage()));
    }
}
