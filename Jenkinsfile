pipeline {
    agent any
    stages {
        stage('Prepare Maven Settings') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS')]) {
                        echo "Using temporary Maven settings file"
                        def persistentSettingsPath = "${env.WORKSPACE}/maven-settings.xml"
                        sh "cp ${MAVEN_SETTINGS} ${persistentSettingsPath}"
                        env.MAVEN_SETTINGS_PATH = persistentSettingsPath
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    sh "mvn clean test -DskipTests=true -s ${env.MAVEN_SETTINGS_PATH}"
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh "mvn clean install -DskipTests=true -s ${env.MAVEN_SETTINGS_PATH}"
                }
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
                script {
                    sh "mvn deploy -DskipTests=true -s ${env.MAVEN_SETTINGS_PATH}"
                }
            }
        }
    }
}
