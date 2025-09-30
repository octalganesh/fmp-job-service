pipeline {
    agent any

    tools {
        jdk 'jdk-11'
        maven 'maven-3'
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
                    if (env.BRANCH_NAME == 'uat') {
                        echo "🚀 Deploying branch: ${env.BRANCH_NAME}"

                        withCredentials([usernamePassword(credentialsId: 'b16783c1-68dc-4c1c-a21a-3cb195001b91', usernameVariable: 'DEPLOY_USER', passwordVariable: 'DEPLOY_PASS')]) {
                            sh '''
                                #!/bin/bash
                                set -e

                                DEPLOY_HOST=192.168.1.44
                                DEPLOY_DIR=/opt/apps/job-service
                                REMOTE_JAR=$DEPLOY_DIR/job-service-0.0.1-SNAPSHOT.jar

                                echo "Finding latest JAR..."
                                JAR_FILE=$(ls -t target/*.jar | head -n1)

                                if [ ! -f "$JAR_FILE" ]; then
                                    echo "❌ ERROR: No JAR found in target/"
                                    exit 1
                                fi

                                echo "Transferring JAR to remote server..."
                                sshpass -p "$DEPLOY_PASS" scp -o StrictHostKeyChecking=no "$JAR_FILE" $DEPLOY_USER@$DEPLOY_HOST:$REMOTE_JAR

                                echo "Running remote deployment commands..."
                                sshpass -p "$DEPLOY_PASS" ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "bash -s" << 'EOF'
                                    set -e
                                    mkdir -p /opt/apps/job-service
                                    chmod 775 /opt/apps/job-service

                                    echo "Stopping old app..."
                                    pkill -f job-service-0.0.1-SNAPSHOT.jar || true

                                    echo "Starting new app..."
                                    nohup java -jar /opt/apps/job-service/job-service-0.0.1-SNAPSHOT.jar \
                                        > /opt/apps/job-service/job-service.log 2>&1 &

                                    sleep 5
                                    PID=$(pgrep -f job-service-0.0.1-SNAPSHOT.jar || true)

                                    if [ -n "$PID" ]; then
                                        echo "✅ Application started with PID $PID"
                                    else
                                        echo "❌ Application failed to start. Check logs at /opt/apps/job-service/job-service.log"
                                        exit 1
                                    fi
EOF
                            '''
                        }
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
