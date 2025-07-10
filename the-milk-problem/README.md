# The milk problem

An example architecture used for managing product inventory which highlights the use of database transactions.

## History

The milk problem first surfaced while working with a well-known grocery store to track product inventory in real time.
The choice of database was largely driven by a non-trivial performance requirement. The initial solution used an 
_eventually consistent_ database which was available and partition tolerant. Read about
the [CAP theorem](https://en.wikipedia.org/wiki/CAP_theorem)
to learn more about the relationship between consistency, availability, and partition tolerance.

The challenge is that high availability comes at the cost of consistency. High availability databases are eventually
consistent, and thus are notorious for _dirty reads_: allowing uncommitted changes from one transaction to affect a read
in another transaction. As a result, the grocery chain was unable to produce an accurate count of milk on the shelves.

The below exercise introduces the reader to transactions while highlighting the challenges of dirty reads.

## The exercise

Get the tests to pass!

- Remove dirty reads.
- Ensure the correct product quantities.

Look for *todo* items in the codebase to get started.

## Quick start

The below steps walk through the environment setup necessary to run the application in both local and production
environments.

### Install dependencies

1. Install PostgreSQL.

   ```bash
   brew install postgresql
   brew services run postgres
   ```

1. Install Flyway.

   ```bash
   brew install flyway
   ```

1. Create a PostgreSQL database.

   ```bash
   createdb
   ```

### Set up the test environment

1. Create the _milk_test_ database.

   ```bash
   psql -c "create database milk_test;"
   psql -c "create user milk with password 'milk';"
   ```

1. Migrate the database with Flyway.

   ```bash
   FLYWAY_CLEAN_DISABLED=false flyway -user=milk -password=milk -url="jdbc:postgresql://localhost:5432/milk_test" -locations=filesystem:databases/milk clean migrate
   ```

### Run tests

Use Gradle to run tests. You'll see a few failures at first.

```bash
./gradlew build
```

### Set up the development environment

1. Create the _milk_development_ database.

   ```bash
   psql -c "create database milk_development;"
   ```

1. Migrate the database with Flyway.

   ```bash
   FLYWAY_CLEAN_DISABLED=false flyway -user=milk -password=milk -url="jdbc:postgresql://localhost:5432/milk_development" -locations=filesystem:databases/milk clean migrate
   ```

1. Source the `.env` file for local development.

   ```bash
   source .env
   ```

1. Populate development data with a product scenario.

   ```bash
   psql -f applications/products-server/src/test/resources/scenarios/products.sql milk_development
   ```

### Run apps

1.  Use Gradle to run the products server

    ```bash
    ./gradlew applications:products-server:run
    ```

1.  Use Gradle to run the simple client

    ```bash
    ./gradlew applications:simple-client:run
    ```

Hope you enjoy the exercise!

Thanks,

The IC Team

© 2023 by Initial Capacity, Inc. All rights reserved.

## 🛠 Note from the Engineer

### Technologies
   __Frameworks, Libraries, and Infrastructure:__
   * __Kotlin__ (JVM language)
   * __Ktor__ (Web server and client)
   * __HikariCP__ (JDBC connection pool)
   * __PostgreSQL__ (Database)
   * __Flyway__ (Database migrations - manages evolution of database schema changes)
   * __Jackson__ (JSON serialization)
   * __Freemarker__ (Server-side templating)
   * __OkHttp__ (HTTP client)
   * __JUnit__ (Testing)
   * __MockK__ (Testing, mocking)
   * __Gradle__ (Build tool)
   * __SLF4J/Logback__ (Logging)

### Systems
   1. __Web Server (Ktor)__
      * __Service__: products-server
      * __Config__:
         * Main entry: applications/products-server/src/main/kotlin/io/milk/start/App.kt
         * Uses Ktor for HTTP server, routing, and middleware.
         * Templating via Freemarker.
         * JSON via Jackson.
         * Logging via SLF4J/Logback.
      * __Hooks__:
         * Connects to PostgreSQL via HikariCP.
         * Reads DB config from environment variables (JDBC_DATABASE_URL, etc.).
   2. __Database (PostgreSQL)__
      __Service__: Used by products-server
      __Config__:
         Connection pool via HikariCP.
         Connection initialized in components/database-support/src/main/kotlin/io/milk/database/DatabaseSupport.kt
         Migrations managed by Flyway (see build.gradle and README.md for CLI usage).
      __Hooks__:
         Used by ProductDataGateway and ProductService.
   3. __Client (Ktor + OkHttp)__
      * __Service__: simple-client
      * __Config__:
          * Main entry: applications/simple-client/src/main/kotlin/io/milk/client/App.kt
          * Uses OkHttp for HTTP requests to the products server.
          * Uses Jackson for JSON serialization.
      * __Hooks__:
          * Reads server URL from PRODUCTS_SERVER env var.

### Powershell commands

   Install postgreSQL
   ```
      --powershell: 
      choco install postgresql -y
      Start-Service postgresql-x64-17
      Get-Service | Where-Object { $_.Name -like "postgres*" }

      psql -U postgres -h localhost -p 5432
      createdb -U milk -h localhost -p 5432 milk_test

      SELECT datname FROM pg_database WHERE datistemplate = false;

      psql -U milk -d milk_development -f C:/Users/pi-user/Desktop/work/projects/software_architecture_for_big_data/the-milk-problem/databases/milk/V1__initial_schema.sql
   ```

   Install flyway
   ```
   scoop install flyway

   // saves a evolution snapshot in the `milk` db - acts like a version control of the db structure
   $env:FLYWAY_CLEAN_DISABLED = "false"
flyway -user=milk -password=milk -url="jdbc:postgresql://localhost:5432/milk_test" -locations="filesystem:C:/Users/pi-user/Desktop/work/projects/software_architecture_for_big_data/the-milk-problem/databases/milk" clean migrate
   ```

   Run products-server
   ```
   // load .env variables
   Get-Content .env | ForEach-Object {
      if ($_ -match '^\s*([^#][^=]+)=(.+)$') {
         $key, $val = $matches[1].Trim(), $matches[2].Trim()
         [System.Environment]::SetEnvironmentVariable($key, $val, "Process")
      }
   }

   // run server
   ./gradlew applications:products-server:run
   ```

