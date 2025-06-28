pipeline {
    agent any

    parameters {
        string(
                name: 'THREADS',
                defaultValue: '6',
                description: 'Количество потоков для параллельного запуска JUnit 5 тестов'
        )
    }

    environment {
        ENV = "qa"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/LovecraftH/Jenkins.git'
            }
        }

        stage('Test') {
            steps {
                script {
                    def threads = params.THREADS?.trim() ? params.THREADS : "6"
                    sh 'chmod +x ./gradlew'
                    sh """
                      ./gradlew clean test
                    """
                }
            }
        }
    }

    post {
        always {
            // Генерация Allure отчёта всегда выполнится
            allure includeProperties: false, jdk: '', results: [[path: 'build/allure-results']]
        }
        failure {
            // Дополнительные действия при падении pipeline
            echo 'Pipeline failed - но Allure отчёт всё равно сгенерирован'
        }
    }
}
