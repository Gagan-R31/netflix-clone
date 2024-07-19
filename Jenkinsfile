pipeline {
    agent any
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub') // Replace 'dockerhub' with your Jenkins credentials ID
        DOCKERHUB_REPO = 'sparshk848/netflix-clone' // Your Docker Hub repository
        IMAGE_TAG = 'netflix-clone' // Image tag, can be changed if needed
        BUILD_TAG = "${env.BUILD_ID}" // Unique tag for each build
        SSH_CREDENTIALS = credentials('ec2-ssh') // Replace 'ec2-ssh' with your Jenkins SSH credentials ID
        EC2_HOST = 'ubuntu@100.27.192.228' // Replace with your EC2 instance's public IP or DNS
    }
    stages {
        stage('Install Docker') {
            steps {
                script {
                    // Install Docker
                    sh '''
                    if ! [ -x "$(command -v docker)" ]; then
                        echo "Docker not found, installing..."
                        curl -fsSL https://get.docker.com -o get-docker.sh
                        sh get-docker.sh
                        sudo usermod -aG docker $USER
                        sudo systemctl start docker
                        sudo chmod 666 /var/run/docker.sock
                    else
                        echo "Docker is already installed"
                    fi
                    '''
                }
            }
        }
        stage('Checkout') {
            steps {
                // Checkout the repository
                checkout scm
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    // Build the Docker image
                    sh """
                    docker build -t ${DOCKERHUB_REPO}:${IMAGE_TAG}-${BUILD_TAG} .
                    """
                }
            }
        }
        stage('Push to Docker Hub') {
            steps {
                script {
                    // Log in to Docker Hub
                    sh """
                    echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                    """
                    // Push the Docker image
                    sh """
                    docker push ${DOCKERHUB_REPO}:${IMAGE_TAG}-${BUILD_TAG}
                    """
                }
            }
        }
        stage('Deploy to EC2') {
            steps {
                script {
                    // SSH into EC2 and run the Docker container
                    sh """
                    ssh -o StrictHostKeyChecking=no -i ${SSH_CREDENTIALS} ${EC2_HOST} <<EOF
                    docker pull ${DOCKERHUB_REPO}:${IMAGE_TAG}-${BUILD_TAG}
                    docker stop netflix-clone || true
                    docker rm netflix-clone || true
                    docker run -d -p 80:80 --name=netflix-clone ${DOCKERHUB_REPO}:${IMAGE_TAG}-${BUILD_TAG}
                    EOF
                    """
                }
            }
        }
    }
    post {
        always {
            script {
                def repoUrl = "https://api.github.com/repos/sparshk380/netflix-clone/statuses/${env.GIT_COMMIT}"
                def status = currentBuild.result == 'SUCCESS' ? 'success' : 'failure'
                
                withCredentials([string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN')]) {
                    sh """
                        curl -H "Authorization: token $GITHUB_TOKEN" \
                             -H "Content-Type: application/json" \
                             -d '{
                                 "state": "${status}",
                                 "target_url": "${env.BUILD_URL}",
                                 "description": "Jenkins Build ${status}",
                                 "context": "jenkins-ci"
                             }' \
                             ${repoUrl}
                    """
                }
            }
        }
    }
}
