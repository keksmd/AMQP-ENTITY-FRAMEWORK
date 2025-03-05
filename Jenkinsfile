pipeline {
 agent { label 'spring-boot-build-image' }
    options {
        skipStagesAfterUnstable()
    }
    stages {

        stage('Test') {

            steps {
                script {
                    sh 'mvn clean test -s $NEXUS_MAVEN_SETTINGS'
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
                    sh 'mvn deploy -s $NEXUS_MAVEN_SETTINGS -DskipTests=true'
                }
            }
        }
    }
}
