pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk   'JDK-17'
    }

    environment {
        MAVEN_OPTS     = '-Dmaven.repo.local=.m2/repository -Xmx1024m'
        DOCKER_HOST    = 'unix:///var/run/docker.sock'
        REGISTRY_HOST  = 'localhost:5000' // При необходимости укажите свой Docker Hub / локальный Registry
        IMAGE_TAG      = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compile & Package Common Models') {
            steps {
                echo '=== Сборка общей библиотеки common-models ==='
                sh 'mvn clean install -DskipTests -pl common-models -am'
            }
        }

        stage('Unit & Component Tests') {
            parallel {
                stage('Order Service Tests') {
                    steps {
                        echo '=== Unit-тесты Order Service ==='
                        sh 'mvn test -pl order-service -Dtest="!RunCucumberTest*"'
                    }
                }
                stage('Inventory Service Tests') {
                    steps {
                        echo '=== Unit-тесты Inventory Service ==='
                        sh 'mvn test -pl inventory-service'
                    }
                }
                stage('Notification Service Tests') {
                    steps {
                        echo '=== Unit-тесты Notification Service ==='
                        sh 'mvn test -pl notification-service'
                    }
                }
            }
        }

        stage('BDD / Integration Tests') {
            steps {
                echo '=== Запуск Cucumber BDD тестов ==='
                sh 'mvn test -pl order-service -Dtest="RunCucumberTest"'
            }
        }

        stage('Build Service JARs') {
            steps {
                echo '=== Упаковка исполняемых JAR-файлов сервисов ==='
                sh 'mvn package -DskipTests -pl order-service,inventory-service,notification-service'
            }
        }

        stage('Build Docker Images') {
            parallel {
                stage('Build Order Service Image') {
                    steps {
                        echo '=== Сборка Docker-образа order-service ==='
                        sh """
                            docker build -f docker/Dockerfile.order-service \
                                         -t order-service:${IMAGE_TAG} \
                                         -t order-service:latest .
                        """
                    }
                }
                stage('Build Inventory Service Image') {
                    steps {
                        echo '=== Сборка Docker-образа inventory-service ==='
                        sh """
                            docker build -f docker/Dockerfile.inventory-service \
                                         -t inventory-service:${IMAGE_TAG} \
                                         -t inventory-service:latest .
                        """
                    }
                }
                stage('Build Notification Service Image') {
                    steps {
                        echo '=== Сборка Docker-образа notification-service ==='
                        sh """
                            docker build -f docker/Dockerfile.notification-service \
                                         -t notification-service:${IMAGE_TAG} \
                                         -t notification-service:latest .
                        """
                    }
                }
            }
        }

        stage('Compose Verification / Smoke Test') {
            steps {
                echo '=== Развертывание и проверка микросервисов и RabbitMQ через docker-compose ==='
                sh """
                    docker compose down --remove-orphans
                    docker compose up -d
                    sleep 15
                    docker compose ps
                """
            }
        }
    }

    post {
        always {
            // Публикация результатов тестов JUnit
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true

            // Публикация отчетов Cucumber (если установлен плагин Cucumber reports)
            cucumber fileIncludePattern: '**/cucumber.json',
            jsonReportDirectory: 'order-service/target/cucumber-reports',
            sortingMethod: 'ALPHABETICAL',
            failedFeaturesNumber: -1,
            failedScenariosNumber: -1,
            failedStepsNumber: -1,
            pendingStepsNumber: -1,
            undefinedStepsNumber: -1,
            missingFails: false,
            noUrlValue: false

            // Сохранение готовых JAR и отчетов в артефакты сборки
            archiveArtifacts artifacts: '**/target/*.jar, **/target/cucumber-reports/**/*', allowEmptyArchive: true

            // Остановка запущенных через compose контейнеров после сборки
            sh 'docker compose down'
        }
        success {
            echo 'Пайплайн завершился успешно: все микросервисы собраны, протестированы и образы готовы.'
        }
        failure {
            echo 'Ошибка на одном из этапов сборки, тестирования или упаковки Docker-образов.'
        }
        cleanup {
            cleanWs()
        }
    }
}