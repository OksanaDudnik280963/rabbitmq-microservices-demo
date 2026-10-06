## Создание Build Pipeline (конвейера сборки)
Создание Build Pipeline (конвейера сборки) в Jenkins для проекта микросервисов 
на Java/Spring Boot и RabbitMQ настраивается с помощью файла Jenkinsfile, 
который уже находится в корневом каталоге репозитория.


## Как создать и запустить этот пайплайн в Jenkins:

## Шаг 1. Подготовка Jenkins и инструментов
Перед созданием пайплайна убедитесь, что в Jenkins настроены необходимые инструменты:

Перейдите в Manage Jenkins -> Tools (Управление Jenkins -> Инструменты).

Убедитесь, что прописаны установщики или пути для:

JDK с именем JDK-17 (так как проект использует Java 17).
XML
Ещё 1

Maven с именем Maven-3.9.


Убедитесь, что на сервере (или агенте Jenkins) установлен Docker и у Jenkins есть права на доступ к демону Docker (/var/run/docker.sock).


## Шаг 2. Создание Pipeline в интерфейсе Jenkins
На главной странице Jenkins нажмите New Item (Создать элемент).


Введите имя проекта, например: rabbitmq-microservices-demo.


Выберите тип проекта Pipeline и нажмите OK.


## Шаг 3. Настройка связи с репозиторием (SCM)
В настройках созданного проекта прокрутите страницу до секции Pipeline.

В выпадающем списке Definition выберите Pipeline script from SCM.


В поле SCM выберите Git.


В поле Repository URL вставьте URL вашего Git-репозитория.


Укажите ветку для сборки (например, */master или */main).


В поле Script Path оставьте значение по умолчанию: Jenkinsfile.


Нажмите Save (Сохранить).


## Шаг 4. Что делает ваш Jenkinsfile
Корневой файл Jenkinsfile автоматически описывает весь процесс сборки (Build Pipeline):


Checkout: Загрузка исходного кода.


Compile & Package Common Models: Сборка общей библиотеки common-models (mvn clean install).


Unit & Component Tests: Параллельный запуск модульных тестов для всех микросервисов (order-service, inventory-service, notification-service).


BDD / Integration Tests: Запуск интеграционных тестов Cucumber с использованием Testcontainers.


Build Service JARs: Упаковка сервисов в исполняемые .jar файлы.


Build Docker Images: Сборка локальных Docker-образов для каждого сервиса.


Compose Verification / Smoke Test: Тестовый запуск всей системы через docker compose up для проверки работоспособности.


## Шаг 5. Запуск и мониторинг пайплайна
На странице вашего проекта в Jenkins нажмите Build Now (Собрать сейчас).


В левом меню в разделе Build History появится новая сборка (например, #1).


Нажмите на номер сборки, затем выберите Console Output (Вывод консоли), чтобы в реальном времени наблюдать за выполнением каждого этапа сборки.


После успешного завершения пайплайна статусы всех этапов станут зелёными, а в разделе сборки появятся отчёты JUnit и Cucumber.
