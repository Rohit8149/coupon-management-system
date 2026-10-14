pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['Staging', 'Production', 'QA'], description: 'Target deployment environment')
        string(name: 'CONTAINER_PORT', defaultValue: '8083', description: 'Host port mapped to container port 8080')
        string(name: 'DOCKER_IMAGE_NAME', defaultValue: 'coupon-app', description: 'Base Docker image name')
        string(name: 'DOCKER_HUB_REPO', defaultValue: 'rohit8149/coupon-app', description: 'Docker Hub repository target')
        string(name: 'REGISTRY_HOST', defaultValue: 'localhost:5000', description: 'Target Docker distribution registry host')
    }

    environment {
        JAVA_HOME = 'C:\\Users\\rohit\\AppData\\Local\\Programs\\Eclipse Adoptium\\jdk-17.0.11.9-hotspot'
        PATH = "${JAVA_HOME}\\bin;${env.PATH}"
        APP_DIR = 'app'
        ARTIFACT_NAME = 'coupon-management-0.0.1-SNAPSHOT.jar'
    }

    stages {
        stage('Checkout SCM') {
            steps {
                echo '=============================================================================='
                echo 'STAGE 1: CHECKOUT SCM'
                echo "Repository: https://github.com/Rohit8149/coupon-management-system.git"
                echo "Branch: ${env.BRANCH_NAME ?: 'main'}"
                echo "Build Number: #${env.BUILD_NUMBER}"
                echo '=============================================================================='
                checkout scm
            }
        }

        stage('Build & Compile') {
            steps {
                echo '=============================================================================='
                echo 'STAGE 2: BUILD & COMPILE'
                echo 'Compiling Java sources and verifying dependencies with Apache Maven'
                echo '=============================================================================='
                dir("${env.APP_DIR}") {
                    bat 'call mvnw.cmd clean compile'
                }
            }
        }

        stage('Automated Test') {
            steps {
                echo '=============================================================================='
                echo 'STAGE 3: AUTOMATED TEST (SELENIUM QUALITY GATE)'
                echo 'Executing JUnit 5 test suite and Spring Boot integration tests'
                echo '=============================================================================='
                dir("${env.APP_DIR}") {
                    bat 'call mvnw.cmd test'
                }
            }
        }

        stage('Package') {
            steps {
                echo '=============================================================================='
                echo 'STAGE 4: PACKAGE'
                echo 'Packaging Spring Boot executable production fat JAR'
                echo '=============================================================================='
                dir("${env.APP_DIR}") {
                    bat 'call mvnw.cmd package -DskipTests=true'
                }
                archiveArtifacts artifacts: 'app/target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build & Versioned Tag') {
            steps {
                echo '=============================================================================='
                echo 'STAGE 5: DOCKER BUILD & VERSIONED TAGGING'
                echo "Building Multi-Stage Docker Image: ${params.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
                echo '=============================================================================='
                bat 'docker build -t ' + params.DOCKER_IMAGE_NAME + ':' + env.BUILD_NUMBER + ' -t ' + params.DOCKER_IMAGE_NAME + ':latest -t ' + params.DOCKER_HUB_REPO + ':' + env.BUILD_NUMBER + ' -t ' + params.DOCKER_HUB_REPO + ':latest -t ' + params.REGISTRY_HOST + '/' + params.DOCKER_IMAGE_NAME + ':' + env.BUILD_NUMBER + ' -t ' + params.REGISTRY_HOST + '/' + params.DOCKER_IMAGE_NAME + ':latest .'
            }
        }

        stage('Publish Image to Registry') {
            steps {
                echo '=============================================================================='
                echo 'STAGE 6: PUBLISH IMAGE TO REGISTRY'
                echo "Pushing versioned image to distribution registry: ${params.REGISTRY_HOST}/${params.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
                echo '=============================================================================='
                bat 'docker push ' + params.REGISTRY_HOST + '/' + params.DOCKER_IMAGE_NAME + ':' + env.BUILD_NUMBER
                bat 'docker push ' + params.REGISTRY_HOST + '/' + params.DOCKER_IMAGE_NAME + ':latest'
            }
        }

        stage('Docker Continuous Deploy') {
            steps {
                echo '=============================================================================='
                echo 'STAGE 7: DOCKER CONTINUOUS DEPLOYMENT (CONTAINER ROLLOUT)'
                echo "Rolling out fresh container on Port ${params.CONTAINER_PORT} (Host: ${params.CONTAINER_PORT} -> Container: 8080)"
                echo "Image Tag: ${params.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
                echo '=============================================================================='
                script {
                    // 1. Teardown any running container instance
                    bat 'powershell -NoProfile -ExecutionPolicy Bypass -Command "try { docker stop coupon-container; docker rm -f coupon-container } catch {}; exit 0"'

                    // 2. Launch fresh container with versioned build tag
                    bat 'docker run -d --name coupon-container -p ' + params.CONTAINER_PORT + ':8080 --restart unless-stopped ' + params.DOCKER_IMAGE_NAME + ':' + env.BUILD_NUMBER

                    // 3. Automated healthcheck verification against Actuator
                    bat 'powershell -NoProfile -ExecutionPolicy Bypass -Command "Write-Host \'Awaiting container readiness on port ' + params.CONTAINER_PORT + '...\'; for ($i = 1; $i -le 30; $i++) { Start-Sleep -Seconds 2; try { $r = Invoke-RestMethod -Uri \'http://localhost:' + params.CONTAINER_PORT + '/actuator/health\' -TimeoutSec 3; if ($r.status -eq \'UP\') { Write-Host \'[SUCCESS] Containerized application successfully deployed and healthy on port ' + params.CONTAINER_PORT + '!\'; exit 0 } } catch {} }; Write-Error \'Health check failed\'; exit 1"'
                }
            }
        }
    }

    post {
        success {
            echo '=============================================================================='
            echo 'JENKINS-DOCKER CONTINUOUS DEPLOYMENT PIPELINE COMPLETED SUCCESSFULLY'
            echo "Versioned Docker Image:  ${params.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
            echo "Docker Hub Tag:          ${params.DOCKER_HUB_REPO}:${env.BUILD_NUMBER}"
            echo "Registry Image:          ${params.REGISTRY_HOST}/${params.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
            echo "Container Name:          coupon-container"
            echo "Host Port Forwarding:    http://localhost:${params.CONTAINER_PORT}"
            echo "Actuator Health Check:   http://localhost:${params.CONTAINER_PORT}/actuator/health"
            echo "Coupons Directory:       http://localhost:${params.CONTAINER_PORT}/coupons"
            echo "Dashboard URL:           http://localhost:${params.CONTAINER_PORT}/dashboard"
            echo '=============================================================================='
        }
        failure {
            echo '=============================================================================='
            echo '[FAILURE] Jenkins-Docker Continuous Deployment Pipeline failed.'
            echo 'Check console log for details.'
            echo '=============================================================================='
        }
    }
}
