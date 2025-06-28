pipeline {
    agent any // Использовать любой доступный агент Jenkins для выполнения pipeline

    // === Параметры, которые можно задавать через UI при запуске сборки ===
    parameters {
        string(
                name: 'THREADS',
                choices: [2, 4, 6, 8],
                defaultValue: 6,
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
                description: 'Теги для запуска конкретной группы тестов'
        )
    }

    // === Глобальные переменные окружения, доступные во всех этапах ===
    environment {
        ENV = "${params.ENVIRONMENT}"            // Окружение для тестов (dev/test/stage/prod)
        BROWSER_TYPE = "${params.BROWSER}"       // Браузер для тестов
        TEST_ENVIRONMENT = "${params.ENVIRONMENT}" // Дублируется для примера использования переменных
    }

    stages {
        // === Этап 1: Настройка окружения и вывод всех параметров сборки ===
        stage('Environment Setup') {
            steps {
                script {
                    // Выводим все параметры, с которыми будет запускаться сборка
                    echo "=== Настройки сборки ==="
                    echo "Окружение: ${params.ENVIRONMENT}"
                    echo "Браузер: ${params.BROWSER}"
                    echo "Количество потоков: ${params.THREADS}"
                    echo "Пропустить тесты: ${params.SKIP_TESTS}"
                    echo "JVM параметры: ${params.JVM_OPTS}"
                    echo "Теги тестов: ${params.TEST_TAGS}"
                    echo "Заметки: ${params.RELEASE_NOTES}"

                    // Устанавливаем читаемое имя и описание для текущей сборки в Jenkins UI
                    currentBuild.displayName = "#${BUILD_NUMBER}-${params.ENVIRONMENT}-${params.BROWSER}"
                    currentBuild.description = "Env: ${params.ENVIRONMENT}, Browser: ${params.BROWSER}, Notes: ${params.RELEASE_NOTES}"
                }
            }
        }

        // === Этап 2: Запуск тестов (если не выбран пропуск) ===
        stage('Test') {
            when {
                expression {
                    return !params.SKIP_TESTS // Выполнять только если не выбран пропуск тестов
                }
            }
            steps {
                script {
                    // Получаем количество потоков из параметра, если не задано — используем 6
                    def threads = params.THREADS?.trim() ? params.THREADS : "6"

                    // Даем права на выполнение gradlew (важно для Linux/Unix)
                    sh 'chmod +x ./gradlew'

                    // Запускаем тесты с передачей всех параметров из UI в виде системных свойств
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

    // === Пост-условия: выполняются после всех этапов, независимо от результата ===
    post {
        always {
            script {
                // Генерируем Allure-отчет, если выбран соответствующий параметр
                if (params.GENERATE_REPORT) {
                    allure includeProperties: false, jdk: '', results: [[path: 'build/allure-results']]
                }
            }
        }
        success {
            // Выводим сообщение об успешном завершении сборки
            echo "✅ Сборка завершена успешно для окружения: ${params.ENVIRONMENT}"
        }
        failure {
            // Выводим сообщение о неудачной сборке
            echo "❌ Сборка завершилась с ошибкой для окружения: ${params.ENVIRONMENT}"
        }
    }
}
