pipeline {
    agent any  // Запускаем pipeline на любом доступном агенте

    // Параметр, который можно ввести в UI Jenkins перед запуском
    parameters {
        string(
                name: 'THREADS',
                defaultValue: '6',
                description: 'Количество потоков для параллельного запуска JUnit 5 тестов'
        )
    }

    tools {
        maven 'Maven 3.8.1'  // Используем предустановленный Maven
    }

    environment {
        ENV = "qa"  // Пример переменной окружения, можно использовать в логике
    }

    stages {
        stage('Checkout') {
            steps {
                // Загружаем проект из Git-репозитория
                git branch: 'main', url: 'https://github.com/your/project.git'
            }
        }

        stage('Test') {
            steps {
                // Получаем значение параметра THREADS или берём 6 по умолчанию
                script {
                    def threads = params.THREADS?.trim() ? params.THREADS : "6"

                    // Запускаем тесты, передаём только нужное число потоков
                    sh """
            mvn clean test \
            -Djunit.jupiter.execution.parallel.config.fixed.parallelism=${threads}
          """
                }
            }
        }

        stage('Allure Report') {
            steps {
                // Подключаем генерацию Allure-отчёта
                allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
            }
        }
    }

    post {
        always {
            // Всегда (при успехе или падении) — добавляем JUnit-отчёты
            junit 'target/surefire-reports/*.xml'

            // Сохраняем лог-файлы и другие артефакты
            archiveArtifacts artifacts: 'target/**/*.log', fingerprint: true
        }
    }
}
