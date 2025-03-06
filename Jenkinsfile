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
                configFileProvider([configFile(fileId: 'MyGlobalSettings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'echo "Using custom Maven settings.xml from Jenkins Config File Management"'
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
