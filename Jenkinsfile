pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)

        buildDiscarder(
            logRotator(
                numToKeepStr: '20',
                artifactNumToKeepStr: '10'
            )
        )
    }

    parameters {
        choice(
            name: 'DEPLOY_ENVIRONMENT',
            choices: ['none', 'staging', 'production'],
            description: 'Deployment target'
        )

        booleanParam(
            name: 'DEPLOY',
            defaultValue: false,
            description: 'Deploy Docker images'
        )
    }

    environment {
        DOCKER_NAMESPACE = 'your-docker-user'

        ORDER_IMAGE =
            "${DOCKER_NAMESPACE}/order-service"

        INVENTORY_IMAGE =
            "${DOCKER_NAMESPACE}/inventory-service"

        NOTIFICATION_IMAGE =
            "${DOCKER_NAMESPACE}/notification-service"

        DOCKER_CREDENTIALS_ID =
            'docker-hub-credentials'

        DEPLOY_SSH_CREDENTIALS_ID =
            'deploy-server-ssh'

        DEPLOY_SERVER =
            'your.server.example.com'

        DEPLOY_USER =
            'deploy'

        DEPLOY_DIRECTORY =
            '/opt/rabbitmq-microservices'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm

                script {
                    env.SHORT_COMMIT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()

                    env.IMAGE_TAG =
                        "${env.BUILD_NUMBER}-${env.SHORT_COMMIT}"

                    echo "Commit: ${env.SHORT_COMMIT}"
                    echo "Image tag: ${env.IMAGE_TAG}"
                }
            }
        }

        stage('Verify Tools') {
            steps {
                sh '''
                    set -eu

                    java -version
                    mvn --version
                    docker version
                '''
            }
        }

        stage('Compile') {
            steps {
                sh '''
                    mvn -B \
                       -DskipTests \
                       clean compile
                '''
            }
        }

        stage('Unit and BDD Tests') {
            steps {
                sh '''
                    mvn -B \
                       -Dspring.profiles.active=test \
                       test
                '''
            }
        }

        stage('Publish Test Reports') {
            steps {
                junit(
                    testResults: '**/target/surefire-reports/*.xml',
                    allowEmptyResults: false
                )

                cucumber(
                    fileIncludePattern: '**/target/cucumber.json',
                    buildStatus: 'UNSTABLE',
                    trendsLimit: 10
                )

                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'order-service/target',
                    reportFiles: 'cucumber-report.html',
                    reportName: 'Cucumber Report'
                ])
            }
        }

        stage('Package JAR Files') {
            steps {
                sh '''
                    mvn -B \
                       -DskipTests \
                       package
                '''

                archiveArtifacts(
                    artifacts: '**/target/*.jar',
                    fingerprint: true
                )
            }
        }

        stage('Build Docker Images') {
            steps {
                sh '''
                    set -eu

                    docker build \
                        --pull \
                        -f docker/Dockerfile.order-service \
                        -t ${ORDER_IMAGE}:${IMAGE_TAG} \
                        .

                    docker build \
                        --pull \
                        -f docker/Dockerfile.inventory-service \
                        -t ${INVENTORY_IMAGE}:${IMAGE_TAG} \
                        .

                    docker build \
                        --pull \
                        -f docker/Dockerfile.notification-service \
                        -t ${NOTIFICATION_IMAGE}:${IMAGE_TAG} \
                        .
                '''
            }
        }

        stage('RabbitMQ Smoke Test') {
            steps {
                sh '''
                    set -eu

                    docker compose up -d rabbitmq

                    sleep 20

                    docker compose ps rabbitmq
                    docker compose logs --tail=100 rabbitmq
                '''
            }

            post {
                always {
                    sh '''
                        docker compose down -v || true
                    '''
                }
            }
        }

        stage('Push Docker Images') {
            when {
                branch 'main'
            }

            steps {
                script {
                    docker.withRegistry(
                        'https://index.docker.io/v1/',
                        env.DOCKER_CREDENTIALS_ID
                    ) {
                        sh '''
                            set -eu

                            docker push \
                                ${ORDER_IMAGE}:${IMAGE_TAG}

                            docker push \
                                ${INVENTORY_IMAGE}:${IMAGE_TAG}

                            docker push \
                                ${NOTIFICATION_IMAGE}:${IMAGE_TAG}
                        '''
                    }
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                allOf {
                    expression {
                        return params.DEPLOY
                    }

                    expression {
                        return params.DEPLOY_ENVIRONMENT == 'staging'
                    }

                    branch 'main'
                }
            }

            steps {
                sshagent(
                    credentials: [env.DEPLOY_SSH_CREDENTIALS_ID]
                ) {
                    sh '''
                        set -eu

                        mkdir -p ~/.ssh
                        chmod 700 ~/.ssh

                        ssh-keyscan -H "${DEPLOY_SERVER}" \
                            >> ~/.ssh/known_hosts

                        ssh "${DEPLOY_USER}@${DEPLOY_SERVER}" \
                            "mkdir -p '${DEPLOY_DIRECTORY}'"

                        scp docker-compose.yml \
                            "${DEPLOY_USER}@${DEPLOY_SERVER}:${DEPLOY_DIRECTORY}/docker-compose.yml"

                        ssh "${DEPLOY_USER}@${DEPLOY_SERVER}" "
                            set -eu

                            cd '${DEPLOY_DIRECTORY}'

                            export DOCKER_NAMESPACE='${DOCKER_NAMESPACE}'
                            export IMAGE_TAG='${IMAGE_TAG}'

                            docker compose pull
                            docker compose up -d --remove-orphans
                            docker compose ps
                        "
                    '''
                }
            }
        }

        stage('Verify Staging Deployment') {
            when {
                allOf {
                    expression {
                        return params.DEPLOY
                    }

                    expression {
                        return params.DEPLOY_ENVIRONMENT == 'staging'
                    }

                    branch 'main'
                }
            }

            steps {
                sshagent(
                    credentials: [env.DEPLOY_SSH_CREDENTIALS_ID]
                ) {
                    sh '''
                        set -eu

                        ssh "${DEPLOY_USER}@${DEPLOY_SERVER}" "
                            curl --fail --retry 10 --retry-delay 5 \
                                http://localhost:8081/api/orders/health

                            curl --fail --retry 10 --retry-delay 5 \
                                http://localhost:8082/api/inventory/health

                            curl --fail --retry 10 --retry-delay 5 \
                                http://localhost:8083/api/notifications/health
                        "
                    '''
                }
            }
        }

        stage('Approve Production Deployment') {
            when {
                allOf {
                    expression {
                        return params.DEPLOY
                    }

                    expression {
                        return params.DEPLOY_ENVIRONMENT == 'production'
                    }

                    branch 'main'
                }
            }

            steps {
                timeout(time: 15, unit: 'MINUTES') {
                    input(
                        message: "Deploy build ${BUILD_NUMBER} to production?",
                        ok: 'Deploy'
                    )
                }
            }
        }

        stage('Deploy to Production') {
            when {
                allOf {
                    expression {
                        return params.DEPLOY
                    }

                    expression {
                        return params.DEPLOY_ENVIRONMENT == 'production'
                    }

                    branch 'main'
                }
            }

            steps {
                sshagent(
                    credentials: [env.DEPLOY_SSH_CREDENTIALS_ID]
                ) {
                    sh '''
                        set -eu

                        mkdir -p ~/.ssh
                        chmod 700 ~/.ssh

                        ssh-keyscan -H "${DEPLOY_SERVER}" \
                            >> ~/.ssh/known_hosts

                        ssh "${DEPLOY_USER}@${DEPLOY_SERVER}" \
                            "mkdir -p '${DEPLOY_DIRECTORY}'"

                        scp docker-compose.yml \
                            "${DEPLOY_USER}@${DEPLOY_SERVER}:${DEPLOY_DIRECTORY}/docker-compose.yml"

                        ssh "${DEPLOY_USER}@${DEPLOY_SERVER}" "
                            set -eu

                            cd '${DEPLOY_DIRECTORY}'

                            export DOCKER_NAMESPACE='${DOCKER_NAMESPACE}'
                            export IMAGE_TAG='${IMAGE_TAG}'

                            docker compose pull
                            docker compose up -d --remove-orphans
                            docker compose ps
                        "
                    '''
                }
            }
        }
    }

    post {
        always {
            junit(
                testResults: '**/target/surefire-reports/*.xml',
                allowEmptyResults: true
            )

            archiveArtifacts(
                artifacts: '**/target/*.jar',
                allowEmptyArchive: true,
                fingerprint: true
            )

            sh '''
                docker compose down -v || true
            '''
        }

        success {
            echo "Pipeline ${BUILD_NUMBER} completed successfully."
            echo "Docker tag: ${IMAGE_TAG}"
        }

        unstable {
            echo 'Pipeline completed with unstable tests or reports.'
        }

        failure {
            echo "Pipeline ${BUILD_NUMBER} failed."
        }

        cleanup {
            sh '''
                docker image prune -f || true
            '''
        }
    }
}