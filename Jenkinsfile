pipeline {
    agent any

    tools {
        jdk 'jdk-11'        // Adjust to your Jenkins JDK name
        maven 'maven-3'     // Adjust to your Jenkins Maven 
    }

    stages {
        stage('Checkout') {
            steps {
                echo "📥 Checking out source code..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "⚙️ Building project with Maven..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'development') {
                        echo "🚀 Deploying branch: ${env.BRANCH_NAME}"

                        sh '''
                            #!/bin/bash
                            set -e

                            # Ensure deployment folder exists and writable
                            mkdir -p /opt/apps/job-service
                            chmod 775 /opt/apps/job-service

                            echo "Stopping old app..."
                            pkill -f job-service-0.0.1-SNAPSHOT.jar || true

                            echo "Finding latest JAR..."
                            JAR_FILE=$(ls -t target/*.jar | head -n1)

                            if [ -f "$JAR_FILE" ]; then
                                echo "Deploying $JAR_FILE to /opt/apps/..."
                                cp "$JAR_FILE" /opt/apps/job-service-0.0.1-SNAPSHOT.jar
                            else
                                echo "❌ ERROR: No JAR found in target/"
                                exit 1
                            fi

                            echo "Starting app with setsid to detach from Jenkins..."
                             setsid  java -jar /opt/apps/job-service-0.0.1-SNAPSHOT.jar \
                                > /opt/apps/job-service/job-service.log 2>&1 < /dev/null &

                            PID=$!
                            echo "App started with PID $PID"

                            # Wait a few seconds to verify
                            sleep 5

                            if ps -p $PID > /dev/null; then
                                echo "✅ Application is running"
                            else
                                echo "❌ Application failed to start. Logs:"
                                head -n50 /opt/apps/job-service/job-service.log
                                exit 1
                            fi
                        '''
                    } else {
                        echo "⏭ Skipping deploy for branch ${env.BRANCH_NAME}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build & deploy successful for branch ${env.BRANCH_NAME}"
        }
        failure {
            echo "❌ Build or deploy failed for branch ${env.BRANCH_NAME}"
        }
    }
}
