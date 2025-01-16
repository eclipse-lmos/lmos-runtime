# lmos-runtime-core

## Overview

The `lmos-runtime-core` is the foundational module of the LMOS Runtime, providing core functionalities for managing conversations and agent interactions. This module includes configurations, models, and services essential for the dynamic routing and handling of conversations in a multi-tenant and multi-channel environment.

## Features
- **Core Agent Model**: Defines models for Agents, Conversations, and their capabilities.
- **Routing Logic**: Implements logic for routing conversations to appropriate agents.
- **Caching**: Provides a caching mechanism for routing information to enhance performance.
- **Extensible Services**: Provides interfaces and default implementations of core services for outbound communication and agent resolution logic like AgentRoutingService, AgentClientService, and AgentRegistryService.



## Usage

### Installation
Make sure to include this module as a dependency in your Gradle build script:
```kotlin
dependencies {
    implementation(project(":lmos-runtime-core:${version}"))
}
```

### Notes
- **Customization**: Extend the core interfaces to add custom agent routing and management logic according to your application's needs.
- Ensure proper configuration of OpenAI or other third-party integrations via `LmosRuntimeConfig`.

### Tests
Run unit tests using JUnit and MockK for simulating conversations and service interactions:
```bash
./gradlew :lmos-runtime-core:test
```