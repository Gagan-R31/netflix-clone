pipeline {
    agent none // Define agents within stages
    environment {
        GITHUB_TOKEN = credentials('github-token1') // Jenkins credentials ID for GitHub token
        IMAGE_TAG = 'unode-onboard-api' // Image tag, can be changed if needed
        BUILD_TAG = "${env.BUILD_ID}" // Unique tag for each build
    }
    stages {
        stage('Prepare Pod Config') {
            agent any // This stage can run on any available node
            steps {
                script {
                    // Load YAML from Jenkins secret and write to a file
                    withCredentials([string(credentialsId: 'k8s-pod-yaml', variable: 'POD_YAML')]) {
                        writeFile file: 'pod-config.yaml', text: POD_YAML
                    }
                }
            }
        }
        stage('Clone Repository') {
            agent {
                kubernetes {
                    label 'k8s-agent'
                    yaml readFile('pod-config.yaml') // Load YAML file for the agent
                }
            }
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
            agent {
                kubernetes {
                    label 'k8s-agent'
                    yaml readFile('pod-config.yaml') // Load YAML file for the agent
                }
            }
            steps {
                container('kaniko') {
                    script {
                        sh '''
                        cd netflix-clone
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
            agent {
                kubernetes {
                    label 'k8s-agent'
                    yaml readFile('pod-config.yaml') // Load YAML file for the agent
                }
            }
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
