# lmos-runtime-spring-boot-starter

## Overview
The LMOS Runtime Spring Boot Starter module provides an easy way to integrate LMOS Runtime's capabilities into a Spring Boot application. This starter simplifies the setup by auto-configuring essential services like agent routing, conversation handling, and caching mechanisms, allowing developers to rapidly develop multi-tenant, multi-channel applications using LMOS Runtime.

## Features
- **Spring Boot Integration**: Simplifies the incorporation of LMOS Runtime into Spring Boot applications.
- **Automatic Configuration**: Auto-configures necessary components for agent management and routing.
- **Property Management**: Enables easy configuration through LmosRuntimeProperties.

### Usage
* Add the starter dependency to your Spring Boot project:

```kotlin
implementation("org.eclipse.lmos:lmos-runtime-spring-boot-starter:${version}")
```

* Once the starter is included in your Spring Boot application, configure it via `application.yaml` or environment variables. Below is a sample `application.yaml` configuration:

```yaml
lmos:
  runtime:
    agent-registry:
      base-url: http://lmos-operator:8080
    router:
      type: EXPLICIT
    open-ai:
      url: https://api.openai.com/v1
      key: <your-api-key>
      model: gpt-3.5-turbo
      max-tokens: 2000
      temperature: 0.0
      format: json_object
    cache:
      ttl: 1800
    cors:
      enabled: true
      allowed-origins: "*"
      allowed-methods: "*"
      allowed-headers: "*"
      patterns: "/**"
      max-age: 8000

server:
  port: 8081
```

## Tests

To run tests specific to this Spring Boot Starter module, execute the following command:

```bash
./gradlew :lmos-runtime-spring-boot-starter:test
```