pipeline {
  agent any

  tools {
    jdk 'jdk-11'        // adjust to your installed JDK
    maven 'maven-3'     // adjust to your Maven name in Jenkins tools
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        sh 'mvn -B clean package'
      }
    }

    stage('Deploy') {
      when {
        branch 'development'   // ✅ only deploy if branch is
      }
      steps {
        sh """
          echo 'Stopping old app...'
          pkill -f 'java.*job-service-0.0.1-SNAPSHOT.jar' || true

          echo 'Deploying new JAR...'
          cp target/*.jar /opt/apps/job-service-0.0.1-SNAPSHOT.jar

          echo 'Starting app...'
          nohup java -jar /opt/apps/job-service-0.0.1-SNAPSHOT.jar > /var/log/job-service.txt 2>&1 &
        """
      }
    }
  }

  post {
    success {
      echo '✅ Build & deploy successful on same server'
    }
    failure {
      echo '❌ Build failed'
    }
  }
}
