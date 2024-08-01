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
    }
    stages {
        stage('Clone Repository') {
            steps {
                script {
                    sh '''
                    git clone -b Test https://${GITHUB_TOKEN}@github.com/Gagan-R31/netflix-clone.git
                    cd netflix-clone
                    '''
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
                        go test -v ./...
                        '''
                    }
                }
            }
        }
        stage('Build Docker Image with Kaniko') {
            steps {
                container('kaniko') {
                    script {
                        sh '''
                        cd undode
                        /kaniko/executor --dockerfile=${WORKSPACE}/your-repo/Dockerfile \
                                         --context=${WORKSPACE}/your-repo \
                                         --destination=${DOCKERHUB_REPO}:${IMAGE_TAG}-${BUILD_TAG}
                        '''
                    }
                }
            }
        }
    }
}
