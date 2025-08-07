properties([disableConcurrentBuilds(abortPrevious: true)])
@Library('maven-lib@1.0.5') _
node {
    cleanWs()
    stage('Checkout') {
        checkout scm
    }

    stage('Build && Test') {
        mvn("clean install -U")
    }
    stage('archieveArtifacts') {
        archieveArtifacts artifacts: '**/target/*.jar', fingerprint: true
    }

    if (!isPR()) {
        stage('MVN Deploy') {
            mvn("deploy -DskipTests=true")
        }
    }
}