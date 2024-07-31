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
        GITHUB_TOKEN = credentials('github-token1') // Jenkins credentials ID for GitHub token
        IMAGE_TAG = 'unode-onboard-api' // Image tag, can be changed if needed
        BUILD_TAG = "${env.BUILD_ID}" // Unique tag for each build
        SOURCE_BRANCH = "${env.CHANGE_BRANCH ?: env.GIT_BRANCH}"
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
