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
                    sh "mvn -Dmaven.repo.local=/root/.m2/repository/ clean test -ntp -U -s ${env.MAVEN_SETTINGS_PATH}"
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh "mvn -Dmaven.repo.local=/root/.m2/repository/ clean install -DskipTests=true -ntp -U -s ${env.MAVEN_SETTINGS_PATH}"
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
                    sh "mvn -Dmaven.repo.local=/root/.m2/repository/ deploy -ntp -U -DskipTests=true -s ${env.MAVEN_SETTINGS_PATH}"
                }
            }
        }
    }
}
