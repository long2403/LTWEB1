pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    options {
        skipDefaultCheckout(true)
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                dir('lab1') {
                    sh 'chmod +x mvnw && ./mvnw -B clean package -DskipTests'
                }
            }
        }

        stage('Deploy to Render') {
            steps {
                withCredentials([string(
                    credentialsId: 'render-deploy-hook',
                    variable: 'RENDER_DEPLOY_HOOK_URL'
                )]) {
                    sh 'curl --fail --silent --show-error --request POST "$RENDER_DEPLOY_HOOK_URL"'
                }
            }
        }
    }
}
