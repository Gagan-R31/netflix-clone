pipeline {
    agent {
        kubernetes {
            label 'k8s-agent'
            yamlFile 'k8s-agent-pod.yaml'
        }
    }
    environment {
        GITHUB_TOKEN = credentials('github-token1') // Jenkins credentials ID for GitHub token
        IMAGE_TAG = 'my-app' // Image tag, can be changed if needed
        BUILD_TAG = "${env.BUILD_ID}" // Unique tag for each build
        SOURCE_BRANCH = "${env.CHANGE_BRANCH ?: env.GIT_BRANCH}"
    }
    stages {
        stage('Clone Repository') {
            steps {
                script {
                    sh '''
                    echo "Cloning branch: ${env.SOURCE_BRANCH}"
                    git clone -b ${env.SOURCE_BRANCH} https://${GITHUB_TOKEN}@github.com/Gagan-R31/netflix-clone.git
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
                        # Testing 
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
                        sh '''
                        cd netflix-clone
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
