# lmos-runtime-service

## Overview
`lmos-runtime-service` specifically focuses on exposing RESTful endpoints to facilitate interaction with the LMOS Runtime.

## Features

- **REST API for Conversation Management**: Provides endpoints for interacting with and managing conversations.
- **Configurable CORS**: Allows detailed configuration of cross-origin resource sharing to ensure secure and efficient communication.
- **Error Handling**: Comprehensive global exception handling to ensure API stability.


## Setup and Installation
### How to install on a Kubernetes cluster:

#### Follow steps to [install lmos-operator](https://github.com/eclipse-lmos/lmos-operator/blob/main/README.md) on kubernetes cluster

Install lmos-runtime

```
helm install lmos-runtime oci://ghcr.io/eclipse-lmos/lmos-runtime-chart --version <current_version>
```

### How to test locally:

#### Follow steps to [install lmos-operator](https://github.com/eclipse-lmos/lmos-operator/blob/main/README.md) on Minikube

Clone and start the lmos-runtime:

```
git clone https://github.com/eclipse-lmos/lmos-runtime
```
update the endpoint of lmos-operator, endpoint and key of openai in [application.yml](src/main/resources/application.yaml)

```
cd lmos-runtime
./gradlew bootRun
```

## Usage
To interact with LMOS Runtime, send a POST request to the chat endpoint:
```
curl -X POST "http://127.0.0.1:<port>/lmos/runtime/apis/v1/<tenant>/chat/<conversationId>/message"
     -H "Content-Type: application/json"
     -H "x-turn-id: <turnId>"
     -d '{
           "inputContext": {
             "messages": [
               {
                 "role": "user",
                 "format": "text",
                 "content": "<user query>"
               }
             ]
           },
           "systemContext": {
             "channelId": "web"
           },
           "userContext": {
             "userId": "user456"
           }
         }'
```

## CORS Configuration

| Property                              | Environment Variable/ Kubernetes ConfigMaps/ Secrets | Description                                     | Default                     |
|---------------------------------------|------------------------------------------------------|-------------------------------------------------|-----------------------------|
| `lmos.runtime.cors.enabled`           | CORS_ENABLED                                         | Enable or disable CORS                         | `false`                     |
| `lmos.runtime.cors.allowed-origins`   | CORS_ALLOWED_ORIGINS                                 | Allowed origins for CORS                       | `*`                         |
| `lmos.runtime.cors.allowed-methods`   | CORS_ALLOWED_METHODS                                 | Allowed HTTP methods for CORS                  | `*`                         |
| `lmos.runtime.cors.allowed-headers`   | CORS_ALLOWED_HEADERS                                 | Allowed headers for CORS                       | `*`                         |
| `lmos.runtime.cors.patterns`          | CORS_PATTERNS                                        | URL patterns to which CORS applies             | `/**`                       |
| `lmos.runtime.cors.max-age`           | CORS_MAX_AGE                                         | Maximum age (in seconds) for CORS preflight    | `8000`                      |