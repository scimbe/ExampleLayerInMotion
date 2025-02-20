FROM mcr.microsoft.com/devcontainers/java:17-bullseye

# Extra tools installieren
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        git \
        curl \
        jq \
        unzip \
        vim \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Maven konfigurieren
COPY maven-settings.xml /usr/share/maven/conf/settings.xml

# Entwicklungs-User erstellen
ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# Entwicklungs-User konfigurieren
RUN if [ "$USER_GID" != "1000" ] || [ "$USER_UID" != "1000" ]; then \
        groupmod --gid $USER_GID $USERNAME \
        && usermod --uid $USER_UID --gid $USER_GID $USERNAME \
        && chown -R $USER_UID:$USER_GID /home/$USERNAME; \
    fi

# Arbeitsverzeichnis setzen
WORKDIR /workspace

# Default Shell auf Bash setzen
SHELL ["/bin/bash", "-c"]
