pipeline {
 agent { label 'spring-boot-build-image' }
    options {
        skipStagesAfterUnstable()
    }
     env.PATH = "${tool 'M3'}/bin:${env.PATH}"
     configFileProvider(
            [configFile(fileId: 'MyGlobalSettings', variable: 'MAVEN_SETTINGS')]) {
            sh 'mvn -s $MAVEN_SETTINGS clean package'
     }
    stages {

        stage('Test') {
            steps {
                script {
                    sh 'mvn clean test -DskipTests'
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh 'mvn clean install -DskipTests'
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
                    sh 'mvn deploy -DskipTests=true'
                }
            }
        }
    }
}
