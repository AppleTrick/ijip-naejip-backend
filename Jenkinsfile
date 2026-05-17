pipeline {
    agent any

    stages {
        stage('Pull') {
            steps {
                sh 'cd /home/ubuntu/project/apps/ijip-naejip/ijip-naejip-backend && git pull origin main'
            }
        }
        stage('Deploy') {
            steps {
                sh 'cd /home/ubuntu/project && docker compose up -d --build ijip-back'
            }
        }
    }

    post {
        success {
            echo '이집내집 Backend 배포 성공'
        }
        failure {
            echo '이집내집 Backend 배포 실패'
        }
    }
}
