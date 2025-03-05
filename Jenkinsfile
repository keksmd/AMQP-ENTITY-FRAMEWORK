pipeline {
 agent { label 'spring-boot-build-image' }
    options {
        skipStagesAfterUnstable()
    }
    stages {

        stage('Test') {
            steps {
                script {
                    sh 'mvn clean test'
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh 'mvn clean install'
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
