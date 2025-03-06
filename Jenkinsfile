pipeline {
    agent { label 'spring-boot-build-image' }

    options {
        skipStagesAfterUnstable()
    }

    environment {
        M3_HOME = tool 'M3'
        PATH = "${M3_HOME}/bin:${env.PATH}"
    }

    stages {
         stage('Prepare Maven Settings') {
            steps {
                script {
                    // Retrieve the stored secret file
                    withCredentials([file(credentialsId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS')]) {
                        echo "Using Maven settings file: $MAVEN_SETTINGS"

                        // Set the settings.xml as default for Maven
                        env.MAVEN_OPTS = "-s $MAVEN_SETTINGS"
                    }
                }
            }
         }

        stage('Test') {
            steps {
                sh 'mvn clean test -DskipTests'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Deploy') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'master'
                }
            }
            steps {
                sh 'mvn deploy -s $MAVEN_SETTINGS -DskipTests=true'
            }
        }
    }
}
