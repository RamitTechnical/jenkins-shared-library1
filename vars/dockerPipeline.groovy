def call(Map config = [:]) {

    pipeline {
        agent any

        stages {

            stage('Build') {
                steps {
                    echo "Building the application"
                    git url: config.gitUrl, branch: config.branch
                }
            }

            stage('Test') {
                steps {
                    echo "Running Test"
                    sh "docker build -t ${config.imageName} ."
                }
            }

            stage('Push') {
                steps {
                    echo "Pushing the application"
                    withCredentials([
                        usernamePassword(
                            credentialsId: config.dockerCredId,
                            usernameVariable: 'dockerHubUser',
                            passwordVariable: 'dockerHubPassword'
                        )
                    ]) {
                        sh """
                           docker tag ${config.imageName} ${dockerHubUser}/${config.imageName}:latest
                           docker login -u ${dockerHubUser} -p ${dockerHubPassword}
                           docker push ${dockerHubUser}/${config.imageName}:latest
                        """
                    }
                }
            }

            stage('Deploy') {
                steps {
                    echo "Deploying Application"
                    sh "docker-compose down && docker-compose up -d"
                }
            }
        }
    }
}
