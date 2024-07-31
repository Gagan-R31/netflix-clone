// vars/k8sAgent.groovy
def call() {
    return '''
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
