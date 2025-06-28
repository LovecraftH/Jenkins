pipeline {
    agent any  // Запускаем pipeline на любом доступном агенте

    // Параметр, доступный через UI
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
                // Клонируем проект из Git
                git branch: 'master', url: 'https://github.com/LovecraftH/Jenkins.git'
            }
        }

        stage('Test') {
            steps {
                script {
                    def threads = params.THREADS?.trim() ? params.THREADS : "6"

                    // Запускаем Gradle тесты с параметром JUnit
                    sh """
                      ./gradlew clean test \
                      -Djunit.jupiter.execution.parallel.config.fixed.parallelism=${threads}
                    """
                }
            }
        }

        stage('Allure Report') {
            steps {
                // Генерация Allure отчёта (предположим, плагин установлен)
                allure includeProperties: false, jdk: '', results: [[path: 'build/allure-results']]
            }
        }
    }

    post {
        always {
            // Сохраняем артефакты (логи и пр.)
            archiveArtifacts artifacts: 'build/**/*.log', fingerprint: true
        }
    }
}
