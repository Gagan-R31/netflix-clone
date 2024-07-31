pipeline {
    agent none
    environment {
        GITHUB_TOKEN = credentials('github-token') // Jenkins credentials ID for GitHub token
        IMAGE_TAG = 'unode-onboard-api' // Image tag, can be changed if needed
        BUILD_TAG = "${env.BUILD_ID}" // Unique tag for each build
    }
    stages {
        stage('Run on Kubernetes Agent') {
            agent {
                kubernetes {
                    label 'k8s-agent'
                    defaultContainer 'jnlp'
                    yamlFile 'k8s-agent.yaml'
                }
            }
            stages {
                stage('Clone Repository') {
                    steps {
                        script {
                            sh '''
                            git clone -b feat-nodes-failed-test https://${GITHUB_TOKEN}@github.com/unification-com/unode-onboard-api.git
                            cd unode-onboard-api
                            '''
                        }
                    }
                }
                stage('Check Go Installation') {
                    steps {
                        container('kaniko') {
                            script {
                                sh '''
                                cd unode-onboard-api
                                # Testing 
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
                                cd unode-onboard-api
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
    }
}
