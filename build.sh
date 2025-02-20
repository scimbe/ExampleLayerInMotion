#!/bin/bash

# ... (vorheriger Inhalt bleibt unverändert)

# Docker Image bauen
build_docker_image() {
    local image_name="motion-system"
    local image_tag="latest"

    log_info "Baue Docker Image ${image_name}:${image_tag}..."

    if ! docker build -t "${image_name}:${image_tag}" .; then
        log_error "Docker Build fehlgeschlagen"
        exit 1
    fi
    log_info "Docker Image erfolgreich erstellt"

    # Modify Docker Image Test
    log_info "Teste Docker Image..."
    if ! docker run --rm --entrypoint java "${image_name}:${image_tag}" -version; then
        log_error "Docker Image Java-Versionstest fehlgeschlagen"
        exit 1
    fi
    log_info "Docker Image Java-Versionstest erfolgreich"
}

# ... (Rest des Skripts bleibt unverändert)
