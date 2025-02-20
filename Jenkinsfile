pipeline {
    agent any

    // Umgebungsvariablen für das Projekt
    environment {
        DOCKER_IMAGE = 'motion-system'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    }

    // Tools, die in der Pipeline verwendet werden
    tools {
        maven 'Maven 3.9.4'
        jdk 'JDK 17'
    }

    // Pipeline-Stufen
    stages {
        // Code auschecken
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // Maven-Abhängigkeiten cachen
        stage('Cache Dependencies') {
            steps {
                cache(maxCacheSize: 250, caches: [
                    arbitraryFileCache(path: '.m2/repository')
                ]) {
                    sh 'mvn dependency:go-offline'
                }
            }
        }

        // Code kompilieren
        stage('Compile') {
            steps {
                sh 'mvn -B clean compile'
            }
        }

        // Unit-Tests ausführen
        stage('Unit Tests') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        // Code-Qualitätsanalyse
        stage('Code Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        // Quality Gate überprüfen
        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // Pakete erstellen
        stage('Package') {
            steps {
                sh 'mvn -B package assembly:single -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*jar-with-dependencies.jar', fingerprint: true
                }
            }
        }

        // Docker-Image bauen
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }

        // Docker-Image testen
        stage('Test Docker Image') {
            steps {
                script {
                    // Container starten und grundlegende Tests durchführen
                    docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").inside {
                        sh 'java -version'
                    }
                }
            }
        }

        // Docker-Image pushen
        stage('Push Docker Image') {
            when {
                branch 'main'  // Nur auf dem main Branch ausführen
            }
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push('latest')
                    }
                }
            }
        }
    }

    // Post-Build-Aktionen
    post {
        always {
            // Workspace aufräumen
            cleanWs()
        }
        success {
            // Benachrichtigung bei Erfolg
            emailext(
                subject: "Pipeline erfolgreich: ${currentBuild.fullDisplayName}",
                body: "Die Build-Pipeline wurde erfolgreich ausgeführt.\n\nDetails: ${env.BUILD_URL}",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
        failure {
            // Benachrichtigung bei Fehler
            emailext(
                subject: "Pipeline fehlgeschlagen: ${currentBuild.fullDisplayName}",
                body: "Die Build-Pipeline ist fehlgeschlagen.\n\nDetails: ${env.BUILD_URL}",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
    }

    // Pipeline-Optionen
    options {
        // Build-Timeout
        timeout(time: 1, unit: 'HOURS')
        
        // Build-Historie
        buildDiscarder(logRotator(numToKeepStr: '10'))
        
        // Gleichzeitige Builds verhindern
        disableConcurrentBuilds()
        
        // Ansi-Farben in der Konsole aktivieren
        ansiColor('xterm')
    }
}