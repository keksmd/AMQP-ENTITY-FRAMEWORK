pipeline {
 agent { label 'spring-boot-build-image' }
    options {
        skipStagesAfterUnstable()
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
