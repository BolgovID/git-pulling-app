# GitHub Repositories API

This project is a Spring Boot application that provides an API to retrieve GitHub repositories for a given user,
including branches and their last commit. The API ensures that the `Accept` header is `application/json`, and appropriate
error responses are returned for unsupported media types and non-existent users.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Api Documentation](#api-documentation)
- [Issues](#issues)
## Features

- Retrieve a list of GitHub repositories that are not fork for a given user.
- Includes branches and their last commit.
- Validates the `Accept` header to ensure it is `application/json`.
- Custom error handling for unsupported `Accept` headers and non-existent users.

## Getting Started

### Requirements

- Java 17 or higher
- Maven

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/yourusername/github-repos-api.git
   cd github-repos-api
   ```
2. Build the project using Maven:
   ```sh
   mvn clean install
   ```

### Configuration

You can configure the application by editing the application.yaml file located in the `src/main/resources`
directory.

Important: Generate Git API Token and paste it to env variables

   ```yaml
    app:
      github:
         url: https://api.github.com
         token: ${GIT_API_TOKEN}
         version: 2022-11-28
   ```

### Running the Application

Run the application using the following command:

```sh
mvn spring-boot:run
```

The application will start and be accessible at http://localhost:8080.

## Testing

### Running Unit Tests

Run the unit tests using the following command:

```sh
mvn test
```

## Api Documentation

You can find api documentation in `swagger-ui.yaml` file located in the `src/main/resources`.

## Issues

There are problems with body messages while getting 404 or 406 error. If you check it by swagger -request body is missing.
Check it by Postman.