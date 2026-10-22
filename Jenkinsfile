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

        stage('Deploy to AWS EC2') {
            when {
                expression { env.BRANCH_NAME == null || env.BRANCH_NAME == 'main' }
            }
            steps {
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'ec2-deploy-key',
                        keyFileVariable: 'EC2_SSH_KEY',
                        usernameVariable: 'EC2_SSH_USER'
                    ),
                    string(
                        credentialsId: 'ec2-host',
                        variable: 'EC2_HOST'
                    )
                ]) {
                    sh '''
                        ssh -i "$EC2_SSH_KEY" \
                            -o BatchMode=yes \
                            -o StrictHostKeyChecking=accept-new \
                            "$EC2_SSH_USER@$EC2_HOST" \
                            'cd ~/LTWEB1 && git pull --ff-only origin main && docker compose up -d --build'
                    '''
                }
            }
        }
    }
}
