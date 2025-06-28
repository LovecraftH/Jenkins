pipeline {
    agent any

    parameters {
        string(
                name: 'THREADS',
                defaultValue: '6',
                description: 'Количество потоков для параллельного запуска JUnit 5 тестов'
        )

        choice(
                name: 'ENVIRONMENT',
                choices: ['dev', 'test', 'stage', 'prod'],
                description: 'Выберите окружение для запуска тестов'
        )

        choice(
                name: 'BROWSER',
                choices: ['chrome', 'firefox', 'edge', 'safari'],
                description: 'Выберите браузер для UI тестов'
        )

        booleanParam(
                name: 'SKIP_TESTS',
                defaultValue: false,
                description: 'Пропустить выполнение тестов'
        )

        booleanParam(
                name: 'GENERATE_REPORT',
                defaultValue: true,
                description: 'Генерировать Allure отчет'
        )

        text(
                name: 'RELEASE_NOTES',
                defaultValue: 'Автоматический запуск тестов',
                description: 'Описание изменений или заметки к релизу'
        )

        string(
                name: 'JVM_OPTS',
                defaultValue: '-Xmx2g -Xms1g',
                description: 'Дополнительные JVM параметры'
        )

        string(
                name: 'TEST_TAGS',
                defaultValue: 'smoke',
                description: 'Теги для запуска конкретной группы тестов'
        )
    }

    environment {
        ENV = "${params.ENVIRONMENT}"
        BROWSER_TYPE = "${params.BROWSER}"
        TEST_ENVIRONMENT = "${params.ENVIRONMENT}"
    }

    stages {
        stage('Environment Setup') {
            steps {
                script {
                    echo "=== Настройки сборки ==="
                    echo "Окружение: ${params.ENVIRONMENT}"
                    echo "Браузер: ${params.BROWSER}"
                    echo "Количество потоков: ${params.THREADS}"
                    echo "Пропустить тесты: ${params.SKIP_TESTS}"
                    echo "JVM параметры: ${params.JVM_OPTS}"
                    echo "Теги тестов: ${params.TEST_TAGS}"
                    echo "Заметки: ${params.RELEASE_NOTES}"

                    currentBuild.displayName = "#${BUILD_NUMBER}-${params.ENVIRONMENT}-${params.BROWSER}"
                    currentBuild.description = "Env: ${params.ENVIRONMENT}, Browser: ${params.BROWSER}, Notes: ${params.RELEASE_NOTES}"
                }
            }
        }

        stage('Test') {
            when {
                expression {
                    return !params.SKIP_TESTS
                }
            }
            steps {
                script {
                    def threads = params.THREADS?.trim() ? params.THREADS : "6"

                    sh 'chmod +x ./gradlew'

                    sh """
                        ./gradlew clean test \\
                        -Djunit.threads=${threads} \\
                        -Dtest.environment=${params.ENVIRONMENT} \\
                        -Dbrowser=${params.BROWSER} \\
                        -Dtest.tags=${params.TEST_TAGS} \\
                        ${params.JVM_OPTS}
                    """
                }
            }
        }
    }

    post {
        always {
            script {
                if (params.GENERATE_REPORT) {
                    allure includeProperties: false, jdk: '', results: [[path: 'build/allure-results']]
                }
            }
        }
        success {
            echo "✅ Сборка завершена успешно для окружения: ${params.ENVIRONMENT}"
        }
        failure {
            echo "❌ Сборка завершилась с ошибкой для окружения: ${params.ENVIRONMENT}"
        }
    }
}
