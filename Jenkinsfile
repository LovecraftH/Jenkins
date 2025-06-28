pipeline {
    agent any

    parameters {
        // Основные параметры сборки
        string(
                name: 'THREADS',
                defaultValue: '6',
                description: 'Количество потоков для параллельного запуска JUnit 5 тестов'
        )

        // Выбор окружения
        choice(
                name: 'ENVIRONMENT',
                choices: ['dev', 'test', 'stage', 'prod'],
                description: 'Выберите окружение для запуска тестов'
        )

        // Выбор браузера для тестов
        choice(
                name: 'BROWSER',
                choices: ['chrome', 'firefox', 'edge', 'safari'],
                description: 'Выберите браузер для UI тестов'
        )

        // Логический параметр
        booleanParam(
                name: 'SKIP_TESTS',
                defaultValue: false,
                description: 'Пропустить выполнение тестов'
        )

        // Логический параметр для отчетности
        booleanParam(
                name: 'GENERATE_REPORT',
                defaultValue: true,
                description: 'Генерировать Allure отчет'
        )

        // Многострочный текст для заметок
        text(
                name: 'RELEASE_NOTES',
                defaultValue: 'Автоматический запуск тестов',
                description: 'Описание изменений или заметки к релизу'
        )

        // Дополнительные JVM параметры
        string(
                name: 'JVM_OPTS',
                defaultValue: '-Xmx2g -Xms1g',
                description: 'Дополнительные JVM параметры'
        )

        // Выбор тег для запуска конкретных тестов
        string(
                name: 'TEST_TAGS',
                defaultValue: 'smoke',
                description: 'Теги для запуска конкретной группы тестов (например: smoke, regression, api)'
        )
    }

    environment {
        ENV = "${params.ENVIRONMENT}"
        BROWSER_TYPE = "${params.BROWSER}"
        TEST_ENVIRONMENT = "${params.ENVIRONMENT}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/LovecraftH/Jenkins.git'
            }
        }

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

                    // Установка имени сборки с параметрами
                    currentBuild.displayName = "#${BUILD_NUMBER}-${params.ENVIRONMENT}-${params.BROWSER}"
                    currentBuild.description = "Env: ${params.ENVIRONMENT}, Browser: ${params.BROWSER}, Notes: ${params.RELEASE_NOTES}"
                }
            }
        }

        stage('Test') {
            when {
                not { params.SKIP_TESTS }
            }
            steps {
                script {
                    def threads = params.THREADS?.trim() ? params.THREADS : "6"

                    sh 'chmod +x ./gradlew'

                    // Передача всех параметров в тесты
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
            echo " Сборка завершена успешно для окружения: ${params.ENVIRONMENT}"
        }
        failure {
            echo " Сборка завершилась с ошибкой для окружения: ${params.ENVIRONMENT}"
        }
    }
}
