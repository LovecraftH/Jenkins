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

    tools {
        gradle 'Gradle 7.5.1'  // Убедись, что у тебя добавлен Gradle в Jenkins (Manage Jenkins → Global Tool Configuration)
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
            // Подключаем JUnit-отчёты Gradle (они по умолчанию в build/test-results/test)
            junit 'build/test-results/test/*.xml'

            // Сохраняем артефакты (логи и пр.)
            archiveArtifacts artifacts: 'build/**/*.log', fingerprint: true
        }
    }
}
