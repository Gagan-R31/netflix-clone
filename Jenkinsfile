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
        GITHUB_TOKEN = credentials('github-token')
        IMAGE_TAG = 'internal'
        SOURCE_BRANCH = "${env.CHANGE_BRANCH ?: env.GIT_BRANCH}"
        DOCKERHUB_REPO = 'mohitini8'
    }
    stages {
        stage('Clone Repository and Get Commit SHA') {
            steps {
                script {
                    sh """
                    echo "Cloning branch: ${env.SOURCE_BRANCH}"
                    git clone -b ${env.SOURCE_BRANCH} https://${GITHUB_TOKEN}@github.com/unification-com/unode-onboard-api.git
                    cd unode-onboard-api
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
                        cd unode-onboard-api
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
                        sh """
                            cd unode-onboard-api
                            /kaniko/executor --dockerfile=./Dockerfile \
                                             --context=. \
                                             --destination=${DOCKERHUB_REPO}/${IMAGE_TAG}:${env.COMMIT_SHA}
                        """
                    }
                }
            }
        }
    }
}
