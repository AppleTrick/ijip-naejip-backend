pipeline {
    agent any

    stages {
        stage('Pull') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    sh '''
                        cd /home/ubuntu/project/apps/ijip-naejip/ijip-naejip-backend
                        git remote set-url origin https://${GIT_USER}:${GIT_TOKEN}@github.com/AppleTrick/ijip-naejip-backend.git
                        git pull origin main
                        git remote set-url origin https://github.com/AppleTrick/ijip-naejip-backend.git
                    '''
                }
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
