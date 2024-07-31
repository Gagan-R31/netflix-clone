@Library('my-shared-library') _

pipeline {
    agent {
        kubernetes {
            label 'k8s-agent'
            yaml k8sAgent()
        }
    }
    environment {
        GITHUB_TOKEN = credentials('github-token1')
        IMAGE_TAG = 'unode-onboard-api'
        SOURCE_BRANCH = "${env.CHANGE_BRANCH ?: env.GIT_BRANCH}"
        DOCKERHUB_REPO = 'gaganr31/jenkins'
    }
    stages {
        stage('Clone Repository and Get Commit SHA') {
            steps {
                script {
                    sh """
                    echo "Cloning branch: ${env.SOURCE_BRANCH}"
                    git clone -b ${env.SOURCE_BRANCH} https://${GITHUB_TOKEN}@github.com/Gagan-R31/netflix-clone.git
                    cd netflix-clone
                    """
                    env.COMMIT_SHA = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    echo "Commit SHA: ${env.COMMIT_SHA}"
                }
            }
        }
        stage('Check Go Installation') {
            steps {
                container('kaniko') {
                    script {
                        sh '''
                        cd netflix-clone
                        which go
                        go version
                        '''
                    }
                }
            }
        }
        stage('Build Docker Image with Kaniko') {
            steps {
                container('kaniko') {
                    script {
                        sh """
                            cd netflix-clone
                            /kaniko/executor --dockerfile=./Dockerfile \
                                             --context=. \
                                             --destination=${DOCKERHUB_REPO}:${IMAGE_TAG}-${env.COMMIT_SHA}
                        """
                    }
                }
            }
        }
    }
}
