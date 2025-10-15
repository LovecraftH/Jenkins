pipeline {
    agent any

    parameters {
        choice(
                name: 'TEST_SUITE',
                choices: ['sequential', 'api', 'ui', 'all'],
                description: 'Какие тесты запустить: sequential (API→UI с условием), api, ui, или all'
        )

        choice(
                name: 'THREADS',
                choices: ['2', '4', '6', '8'],
                description: 'Количество потоков для параллельного запуска тестов'
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

        string(
                name: 'MIN_API_PASS_RATE',
                defaultValue: '80',
                description: 'Минимальный % прохождения API тестов для запуска UI (только для sequential)'
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
                    echo "Тест-сьют: ${params.TEST_SUITE}"
                    echo "Окружение: ${params.ENVIRONMENT}"
                    echo "Браузер: ${params.BROWSER}"
                    echo "Количество потоков: ${params.THREADS}"
                    echo "Минимальный API pass rate: ${params.MIN_API_PASS_RATE}%"
                    echo "Заметки: ${params.RELEASE_NOTES}"

                    currentBuild.displayName = "#${BUILD_NUMBER}-${params.TEST_SUITE}-${params.ENVIRONMENT}"
                    currentBuild.description = "Suite: ${params.TEST_SUITE}, Env: ${params.ENVIRONMENT}, Browser: ${params.BROWSER}"
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('API Tests') {
            when {
                allOf {
                    expression { !params.SKIP_TESTS }
                    expression {
                        params.TEST_SUITE == 'api' ||
                                params.TEST_SUITE == 'sequential' ||
                                params.TEST_SUITE == 'all'
                    }
                }
            }
            steps {
                script {
                    echo "🚀 Запуск API тестов..."

                    sh 'chmod +x ./gradlew'

                    // Очищаем предыдущие результаты
                    sh './gradlew clean'

                    // Запускаем API тесты
                    def apiTestResult = sh(
                            script: """
                            ./gradlew apiTest \\
                            -Djunit.threads=${params.THREADS} \\
                            -Dtest.environment=${params.ENVIRONMENT} \\
                            -Dbrowser=${params.BROWSER} \\
                            --no-daemon \\
                            --console=plain
                        """,
                            returnStatus: true
                    )

                    // Сохраняем статус для следующих стадий
                    env.API_TEST_STATUS = apiTestResult.toString()

                    if (apiTestResult != 0) {
                        echo "⚠️  API тесты завершились с ошибками (код: ${apiTestResult})"
                        // Не прерываем pipeline, продолжаем
                    } else {
                        echo "✅ API тесты прошли успешно"
                    }
                }
            }
            post {
                always {
                    // Публикуем JUnit результаты API тестов
                    junit testResults: '**/build/test-results/apiTest/*.xml',
                            allowEmptyResults: true
                }
            }
        }

        stage('Evaluate API Results') {
            when {
                allOf {
                    expression { !params.SKIP_TESTS }
                    expression { params.TEST_SUITE == 'sequential' }
                    expression { currentBuild.currentResult != 'ABORTED' }
                }
            }
            steps {
                script {
                    echo "📊 Анализ результатов API тестов..."

                    def testResultAction = currentBuild.rawBuild.getAction(
                            hudson.tasks.junit.TestResultAction.class
                    )

                    if (testResultAction != null) {
                        def totalTests = testResultAction.totalCount
                        def failedTests = testResultAction.failCount
                        def passedTests = totalTests - failedTests
                        def passRate = totalTests > 0 ? (passedTests / totalTests) * 100 : 0

                        echo """
=== Результаты API тестов ===
Всего тестов: ${totalTests}
Прошло: ${passedTests}
Упало: ${failedTests}
Success Rate: ${String.format('%.2f', passRate)}%
Минимальный порог: ${params.MIN_API_PASS_RATE}%
                        """

                        env.API_PASS_RATE = String.format('%.2f', passRate)
                        env.API_TOTAL_TESTS = totalTests.toString()
                        env.API_PASSED_TESTS = passedTests.toString()

                        def minPassRate = params.MIN_API_PASS_RATE.toDouble()
                        env.RUN_UI_TESTS = (passRate >= minPassRate) ? 'true' : 'false'

                        if (passRate >= minPassRate) {
                            echo "✅ Success rate (${String.format('%.2f', passRate)}%) >= ${minPassRate}%, UI тесты будут запущены"
                        } else {
                            echo "❌ Success rate (${String.format('%.2f', passRate)}%) < ${minPassRate}%, UI тесты будут пропущены"
                        }
                    } else {
                        echo "⚠️  Результаты тестов не найдены, UI тесты будут запущены"
                        env.RUN_UI_TESTS = 'true'
                    }
                }
            }
        }

        stage('UI Tests') {
            when {
                allOf {
                    expression { !params.SKIP_TESTS }
                    anyOf {
                        expression { params.TEST_SUITE == 'ui' }
                        expression { params.TEST_SUITE == 'all' }
                        expression {
                            params.TEST_SUITE == 'sequential' && env.RUN_UI_TESTS == 'true'
                        }
                    }
                }
            }
            steps {
                script {
                    echo "🚀 Запуск UI тестов..."

                    // НЕ очищаем build/allure-results - там лежат результаты API тестов!
                    def uiTestResult = sh(
                            script: """
                            ./gradlew uiTest \\
                            -Djunit.threads=${params.THREADS} \\
                            -Dtest.environment=${params.ENVIRONMENT} \\
                            -Dbrowser=${params.BROWSER} \\
                            --no-daemon \\
                            --console=plain
                        """,
                            returnStatus: true
                    )

                    env.UI_TEST_STATUS = uiTestResult.toString()

                    if (uiTestResult != 0) {
                        echo "⚠️  UI тесты завершились с ошибками (код: ${uiTestResult})"
                    } else {
                        echo "✅ UI тесты прошли успешно"
                    }
                }
            }
            post {
                always {
                    // Публикуем JUnit результаты UI тестов
                    junit testResults: '**/build/test-results/uiTest/*.xml',
                            allowEmptyResults: true
                }
            }
        }

        stage('Run All Tests') {
            when {
                allOf {
                    expression { !params.SKIP_TESTS }
                    expression { params.TEST_SUITE == 'all' }
                }
            }
            steps {
                script {
                    echo "🚀 Запуск всех тестов (API + UI параллельно)..."

                    sh 'chmod +x ./gradlew'
                    sh './gradlew clean'

                    sh """
                        ./gradlew test \\
                        -Djunit.threads=${params.THREADS} \\
                        -Dtest.environment=${params.ENVIRONMENT} \\
                        -Dbrowser=${params.BROWSER} \\
                        --no-daemon \\
                        --console=plain
                    """
                }
            }
            post {
                always {
                    junit testResults: '**/build/test-results/test/*.xml',
                            allowEmptyResults: true
                }
            }
        }
    }

    post {
        always {
            script {
                // Генерируем единый Allure отчёт из всех результатов
                if (params.GENERATE_REPORT) {
                    echo "📊 Генерация Allure отчёта..."

                    allure includeProperties: false,
                            jdk: '',
                            results: [[path: 'build/allure-results']]
                }

                // Выводим итоговую статистику
                if (params.TEST_SUITE == 'sequential') {
                    echo """
=== ИТОГОВАЯ СТАТИСТИКА ===
API тесты: ${env.API_TOTAL_TESTS ?: 'N/A'} (прошло: ${env.API_PASSED_TESTS ?: 'N/A'}, success rate: ${env.API_PASS_RATE ?: 'N/A'}%)
UI тесты: ${env.RUN_UI_TESTS == 'true' ? 'Запущены' : 'Пропущены'}
                    """
                }
            }
        }

        success {
            echo "✅ Сборка завершена успешно для окружения: ${params.ENVIRONMENT}"
        }

        failure {
            echo "❌ Сборка завершилась с ошибкой для окружения: ${params.ENVIRONMENT}"
        }

        unstable {
            echo "⚠️  Сборка нестабильна (есть упавшие тесты)"
        }
    }
}
