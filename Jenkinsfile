pipeline {
    agent {
        kubernetes {
            label 'k8s-agent'
            yaml '''
            apiVersion: v1
            kind: Pod
            metadata:
              labels:
                some-label: some-label-value
            spec:
              containers:
              - name: jnlp
                image: jenkins/inbound-agent
                args: ['$(JENKINS_SECRET)', '$(JENKINS_NAME)']
              - name: kaniko
                image: gaganr31/kaniko-go
                command:
                - /busybox/sh
                tty: true
                volumeMounts:
                - name: kaniko-secret
                  mountPath: /kaniko/.docker
                - name: workspace-volume
                  mountPath: /workspace
              volumes:
              - name: kaniko-secret
                secret:
                  secretName: kaniko-secret
                  items:
                  - key: .dockerconfigjson
                    path: config.json
              - name: workspace-volume
                emptyDir: {}
            '''
        }
    }
    environment {
        GITHUB_TOKEN = credentials('github-token1')
        IMAGE_TAG = 'unode-onboard-api'
        SOURCE_BRANCH = "${env.CHANGE_BRANCH ?: env.GIT_BRANCH}"
        DOCKERHUB_REPO = 'gaganr31/jenkins'
    }
    stages {
        stage('Clone Repository') {
            steps {
                script {
                    sh """
                    echo "Cloning branch: ${env.SOURCE_BRANCH}"
                    git clone -b ${env.SOURCE_BRANCH} https://${GITHUB_TOKEN}@github.com/Gagan-R31/netflix-clone.git
                    cd netflix-clone
                    """
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
                        // Get the commit SHA
                        def commitSha = sh(script: "cd netflix-clone && git rev-parse --short HEAD", returnStdout: true).trim()
                        
                        // Build and push the image
                        sh """
                            cd netflix-clone
                            /kaniko/executor --dockerfile=./Dockerfile \
                                             --context=. \
                                             --destination=${DOCKERHUB_REPO}:${IMAGE_TAG}-${commitSha}
                        """
                    }
                }
            }
        }
    }
}
