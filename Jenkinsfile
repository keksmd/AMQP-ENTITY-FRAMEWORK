pipeline {
    agent { label 'spring-boot-build-image' }

    options {
        skipStagesAfterUnstable()
    }

    environment {
        M3_HOME = tool 'M3'
        PATH = "${M3_HOME}/bin:${env.PATH}"
        MAVEN_SETTINGS_PATH = '' // Placeholder for Maven settings path
    }

    stages {
        stage('Prepare Maven Settings') {
            steps {
                script {
                    // Retrieve the stored secret file
                    withCredentials([file(credentialsId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS')]) {
                        echo "Using temporary Maven settings file: $MAVEN_SETTINGS"

                        // Copy settings.xml to a persistent location in the workspace
                        def persistentSettingsPath = "${WORKSPACE}/maven-settings.xml"
                        sh "cp $MAVEN_SETTINGS ${persistentSettingsPath}"

                        // Set the path as an environment variable for later stages
                        env.MAVEN_SETTINGS_PATH = persistentSettingsPath
                    }
                }
            }
        }

        stage('Test') {
            steps {
                sh 'mvn clean test -DskipTests=true -s $MAVEN_SETTINGS_PATH'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests=true -s $MAVEN_SETTINGS_PATH'
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
                sh 'mvn deploy -DskipTests=true -s $MAVEN_SETTINGS_PATH'
            }
        }
    }
}
