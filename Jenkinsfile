pipeline {
    agent {
        kubernetes {
            label 'k8s-agent'
            yaml env.KUBERNETES_YAML
        }
    }
    environment {
        KUBERNETES_YAML = credentials('kubernetes-yaml-credentials') // Hidden parameter
        GITHUB_TOKEN = credentials('github-token1')
        IMAGE_TAG = 'unode-onboard-api'
        BUILD_TAG = "${env.BUILD_ID}"
        REPO_OWNER = 'Gagan-R31'
        REPO_NAME = 'netflix-clone'
    }
    stages {
        stage('Clone Repository') {
            steps {
                script {
                    updateGitHubStatus('pending', 'Cloning repository')
                    sh '''
                    git clone -b Test https://${GITHUB_TOKEN}@github.com/Gagan-R31/netflix-clone.git
                    cd netflix-clone
                    '''
                    updateGitHubStatus('success', 'Repository cloned')
                }
            }
        }
        stage('Check Go Installation') {
            steps {
                container('kaniko') {
                    script {
                        updateGitHubStatus('pending', 'Checking Go installation and running tests')
                        sh '''
                        cd netflix-clone
                        which go
                        go version
                        go test -v ./...
                        '''
                        updateGitHubStatus('success', 'Go checks and tests passed')
                    }
                }
            }
        }
        stage('Build Docker Image with Kaniko') {
            steps {
                container('kaniko') {
                    script {
                        updateGitHubStatus('pending', 'Building Docker image')
                        sh '''
                        cd undode
                        /kaniko/executor --dockerfile=${WORKSPACE}/your-repo/Dockerfile \
                                         --context=${WORKSPACE}/your-repo \
                                         --destination=${DOCKERHUB_REPO}:${IMAGE_TAG}-${BUILD_TAG}
                        '''
                        updateGitHubStatus('success', 'Docker image built')
                    }
                }
            }
        }
    }
    post {
        failure {
            script {
                updateGitHubStatus('failure', 'Pipeline failed')
            }
        }
        success {
            script {
                updateGitHubStatus('success', 'Pipeline completed successfully')
            }
        }
    }
}

def updateGitHubStatus(state, description) {
    def commitSha = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    sh """
        curl -H "Authorization: token ${GITHUB_TOKEN}" \
             -H "Accept: application/vnd.github.v3+json" \
             -X POST \
             -d '{"state": "${state}", "description": "${description}", "context": "Jenkins", "target_url": "${env.BUILD_URL}console"}' \
             https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/statuses/${commitSha}
    """
}
