pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['Staging', 'Production', 'QA'], description: 'Target deployment environment')
        string(name: 'SERVER_PORT', defaultValue: '8082', description: 'Application HTTP server port for deployment')
        string(name: 'APP_PROFILE', defaultValue: 'prod', description: 'Active Spring Boot runtime profile')
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
                echo "Deployment Target: ${params.DEPLOY_ENV} (Port: ${params.SERVER_PORT})"
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
                echo 'STAGE 3: AUTOMATED TEST'
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

        stage('Deploy') {
            steps {
                echo '=============================================================================='
                echo 'STAGE 5: DEPLOY'
                echo "Deploying to Embedded Tomcat Web Server on Port ${params.SERVER_PORT}"
                echo "Environment: ${params.DEPLOY_ENV} | Profile: ${params.APP_PROFILE}"
                echo '=============================================================================='
                script {
                    // 1. Terminate any previous instance on the target port (safe exit 0)
                    bat 'powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Get-NetTCPConnection -LocalPort ' + params.SERVER_PORT + ' -State Listen -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue } } catch {}; exit 0"'

                    // 2. Launch production artifact in background using parameterized port
                    bat 'powershell -NoProfile -ExecutionPolicy Bypass -Command "Start-Process -FilePath \'' + env.JAVA_HOME + '\\bin\\java.exe\' -ArgumentList \'-Dserver.port=' + params.SERVER_PORT + '\', \'-jar\', \'app\\target\\' + env.ARTIFACT_NAME + '\' -WindowStyle Hidden; exit 0"'

                    // 3. Health check verification against Actuator endpoint
                    bat 'powershell -NoProfile -ExecutionPolicy Bypass -Command "Write-Host \'Awaiting server readiness on port ' + params.SERVER_PORT + '...\'; for ($i = 1; $i -le 30; $i++) { Start-Sleep -Seconds 2; try { $r = Invoke-RestMethod -Uri \'http://localhost:' + params.SERVER_PORT + '/actuator/health\' -TimeoutSec 3; if ($r.status -eq \'UP\') { Write-Host \'[SUCCESS] Application successfully deployed and healthy on port ' + params.SERVER_PORT + '!\'; exit 0 } } catch {} }; Write-Error \'Health check failed\'; exit 1"'
                }
            }
        }
    }

    post {
        success {
            echo '=============================================================================='
            echo 'PIPELINE AS CODE EXECUTION COMPLETED SUCCESSFULLY'
            echo "Application Deployed URL: http://localhost:${params.SERVER_PORT}"
            echo "Actuator Health Check:    http://localhost:${params.SERVER_PORT}/actuator/health"
            echo "Coupons Directory:        http://localhost:${params.SERVER_PORT}/coupons"
            echo "Summary Dashboard:        http://localhost:${params.SERVER_PORT}/dashboard"
            echo '=============================================================================='
        }
        failure {
            echo '=============================================================================='
            echo '[FAILURE] Pipeline failed. Check console log for details.'
            echo '=============================================================================='
        }
    }
}
