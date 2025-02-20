pipeline {
    agent any

    // Umgebungsvariablen für das Projekt
    environment {
        DOCKER_IMAGE = 'motion-system'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        SONAR_TOKEN = credentials('sonar-token')
        SONAR_PROJECT_KEY = 'com.example:motion-system'
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
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/test/**'
                    )
                }
            }
        }

        // Prepare SonarQube Analysis
        stage('Prepare SonarQube') {
            steps {
                script {
                    def scannerHome = tool 'SonarScanner'
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            ${scannerHome}/bin/sonar-scanner \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.projectName='Character Motion System' \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            -Dsonar.junit.reportPaths=target/surefire-reports \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.verbose=false
                        """
                    }
                }
            }
        }

        // Wait for SonarQube Quality Gate
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // Perform Detailed Code Analysis
        stage('Detailed Analysis') {
            steps {
                script {
                    def sonarResults = sonarGetMetricNames()
                    echo "Available metrics: ${sonarResults}"
                    
                    // Get specific metrics
                    def coverage = sonarGetMetric(projectKey: SONAR_PROJECT_KEY, metricKeys: 'coverage')
                    def bugs = sonarGetMetric(projectKey: SONAR_PROJECT_KEY, metricKeys: 'bugs')
                    def vulnerabilities = sonarGetMetric(projectKey: SONAR_PROJECT_KEY, metricKeys: 'vulnerabilities')
                    
                    echo """
                        Code Analysis Results:
                        - Coverage: ${coverage}%
                        - Bugs: ${bugs}
                        - Vulnerabilities: ${vulnerabilities}
                    """
                    
                    // Set build status based on thresholds
                    if (coverage < 80) {
                        unstable('Code coverage is below 80%')
                    }
                    if (bugs > 0 || vulnerabilities > 0) {
                        unstable('Quality issues found')
                    }
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
                expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
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
            // Save SonarQube results
            script {
                def sonarPublisher = new hudson.plugins.sonar.SonarPublisher()
                sonarPublisher.setBranch(env.BRANCH_NAME)
                currentBuild.rawBuild.setResult(sonarPublisher.perform(currentBuild.rawBuild, launcher, listener))
            }
            
            // Workspace aufräumen
            cleanWs()
        }
        success {
            // Benachrichtigung bei Erfolg
            emailext(
                subject: "Pipeline erfolgreich: ${currentBuild.fullDisplayName}",
                body: """
                    Die Build-Pipeline wurde erfolgreich ausgeführt.
                    
                    SonarQube-Analyse:
                    - Projekt: ${SONAR_PROJECT_KEY}
                    - Dashboard: ${SONAR_HOST_URL}/dashboard?id=${SONAR_PROJECT_KEY}
                    
                    Build-Details: ${env.BUILD_URL}
                """,
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
        failure {
            // Benachrichtigung bei Fehler
            emailext(
                subject: "Pipeline fehlgeschlagen: ${currentBuild.fullDisplayName}",
                body: """
                    Die Build-Pipeline ist fehlgeschlagen.
                    
                    SonarQube-Analyse:
                    - Projekt: ${SONAR_PROJECT_KEY}
                    - Dashboard: ${SONAR_HOST_URL}/dashboard?id=${SONAR_PROJECT_KEY}
                    
                    Build-Details: ${env.BUILD_URL}
                    
                    Bitte überprüfen Sie die Code-Qualität und die Testergebnisse.
                """,
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

        // SonarQube timeout
        timeout(time: 1, unit: 'HOURS') {
            // This ensures the pipeline fails if SonarQube analysis takes too long
            waitForQualityGate abortPipeline: true
        }
    }
}