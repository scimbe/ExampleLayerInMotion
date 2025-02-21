package com.example.motion.api;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.behavior.IMotionLayer;
import com.example.motion.sys.behavior.BasicWalkingLayer;
import com.example.motion.api.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LayerControllerTest {

    @Mock
    private ICharacterMotionService motionService;

    private LayerController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new LayerController(motionService);
    }

    @Test
    void getActiveLayers_ShouldReturnLayerList() {
        // Arrange
        IMotionLayer layer1 = new BasicWalkingLayer();
        List<IMotionLayer> layers = Arrays.asList(layer1);
        when(motionService.getActiveLayers()).thenReturn(layers);

        // Act
        ResponseEntity<List<LayerResponse>> response = controller.getActiveLayers();

        // Assert
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("BasicWalkingLayer", response.getBody().get(0).getName());
    }

    @Test
    void addLayer_ShouldAddNewLayer() {
        // Arrange
        LayerRequest request = new LayerRequest();
        request.setClassName("com.example.motion.sys.behavior.BasicWalkingLayer");
        request.setPriority(1);

        when(motionService.addMotionLayer(any(IMotionLayer.class), eq(1)))
            .thenReturn(true);

        // Act
        ResponseEntity<LayerResponse> response = controller.addLayer(request);

        // Assert
        assertNotNull(response.getBody());
        assertEquals("BasicWalkingLayer", response.getBody().getName());
        assertEquals(request.getClassName(), response.getBody().getClassName());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void addLayer_ShouldHandleExistingLayer() {
        // Arrange
        LayerRequest request = new LayerRequest();
        request.setClassName("com.example.motion.sys.behavior.BasicWalkingLayer");
        request.setPriority(1);

        when(motionService.addMotionLayer(any(IMotionLayer.class), eq(1)))
            .thenReturn(false);

        // Act
        ResponseEntity<LayerResponse> response = controller.addLayer(request);

        // Assert
        assertNotNull(response.getBody());
        assertEquals("Layer already exists", response.getBody().getName());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void removeLayer_ShouldRemoveExistingLayer() {
        // Arrange
        String className = "com.example.motion.sys.behavior.BasicWalkingLayer";
        when(motionService.removeMotionLayer(any(IMotionLayer.class)))
            .thenReturn(true);

        // Act
        ResponseEntity<Void> response = controller.removeLayer(className);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void removeLayer_ShouldHandleNonExistentLayer() {
        // Arrange
        String className = "com.example.motion.sys.behavior.BasicWalkingLayer";
        when(motionService.removeMotionLayer(any(IMotionLayer.class)))
            .thenReturn(false);

        // Act
        ResponseEntity<Void> response = controller.removeLayer(className);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void updateLayerPriority_ShouldUpdatePriority() {
        // Arrange
        String className = "com.example.motion.sys.behavior.BasicWalkingLayer";
        PriorityRequest request = new PriorityRequest();
        request.setPriority(2);

        when(motionService.updateLayerPriority(any(IMotionLayer.class), eq(2)))
            .thenReturn(true);

        // Act
        ResponseEntity<LayerResponse> response = controller.updateLayerPriority(className, request);

        // Assert
        assertNotNull(response.getBody());
        assertEquals("BasicWalkingLayer", response.getBody().getName());
    }

    @Test
    void updateLayerPriority_ShouldHandleNonExistentLayer() {
        // Arrange
        String className = "com.example.motion.sys.behavior.BasicWalkingLayer";
        PriorityRequest request = new PriorityRequest();
        request.setPriority(2);

        when(motionService.updateLayerPriority(any(IMotionLayer.class), eq(2)))
            .thenReturn(false);

        // Act
        ResponseEntity<LayerResponse> response = controller.updateLayerPriority(className, request);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void addLayer_ShouldHandleInvalidClassName() {
        // Arrange
        LayerRequest request = new LayerRequest();
        request.setClassName("invalid.class.name");
        request.setPriority(1);

        // Act
        ResponseEntity<LayerResponse> response = controller.addLayer(request);

        // Assert
        assertNotNull(response.getBody());
        assertEquals("Error creating layer", response.getBody().getName());
    }
}
