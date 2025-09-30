# lmos-runtime
[![Build Status](https://github.com/eclipse-lmos/lmos-runtime/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/eclipse-lmos/lmos-runtime/actions/workflows/gradle.yml)
[![Gradle Package](https://github.com/eclipse-lmos/lmos-runtime/actions/workflows/gradle-publish.yml/badge.svg?branch=main)](https://github.com/eclipse-lmos/lmos-runtime/actions/workflows/gradle-publish.yml)
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)

LMOS Runtime is a component of the LMOS (Language Model Operating System) ecosystem, designed to facilitate dynamic agent routing and conversation handling in a multi-tenant, multi-channel environment.

It is a comprehensive system designed to manage and route conversations to the most suitable agents based on their capabilities. 
It leverages the LMOS Operator and LMOS Router to dynamically resolve and route user queries to the appropriate agents.
This project extends the functionalities of the original [lmos-operator](https://github.com/eclipse-lmos/lmos-operator/)  and [lmos-router](https://github.com/eclipse-lmos/lmos-router) by integrating them into a cohesive runtime environment.

The LMOS Runtime directs user queries to an agent based on its capabilities. 
It fetches the list of all installed agents applicable to the tenant and channel from lmos-operator, and uses lmos-router to dynamically resolve the most appropriate agent for each query. 
The user queries are then forwarded to the selected agent, and the response is returned to the client.

## Features

- **Dynamic Agent Routing** : Routes user queries to the most appropriate agent dynamically.
- **Scalability**: Designed to handle **Multi-tenant** and **multi-channel** efficiently.
- **Extensible Architecture**: Easily extendable for adding more agents and routing logic.

```mermaid
graph TD
    Client[Client] --> |HTTP Request| CC[ConversationController]
    subgraph Kubernetes[Kubernetes Cluster]
        subgraph LMOS_Ecosystem[LMOS Ecosystem]
            CC --> |Process Conversation| CS[ConversationService]
            CS --> |Get Agents| ARS[AgentRegistryService]
            CS --> |Resolve Agent| ARTS[AgentRoutingService]
            CS --> |Ask Agent| ACS[AgentClientService]
            ARS --> |HTTP Request| LO[LMOS Operator]
            ACS --> |WebSocket| Agent1[Service Agent]
            ACS --> |WebSocket| Agent2[Sales Agent]
            ACS --> |WebSocket| Agent3[Technical Support Agent]
            subgraph LMOS_Runtime[LMOS Runtime]
                CC
                CS
                ARS
                ARTS
                ACS
            end
            subgraph ARTS[AgentRoutingService]
                LMOS_Router[LMOS-Router]
            end
        end
        Config[ConfigMaps/Secrets]
        K8S_API[Kubernetes API]
        ARTS --> |Use| LLM_SelfHosted[LLM - Meta LLama]
    end
    Config ---> |Configure| LMOS_Runtime
    ARTS --> |Use| LLM_External[LLM - OpenAI]
    LO <--> |CRUD Operations| K8S_API
    style Kubernetes fill:#e6f3ff,stroke:#4da6ff
    style ARS fill:#e6e6fa,stroke:#9370db,color:#000000
    style ACS fill:#e6e6fa,stroke:#9370db,color:#000000
    style ARTS fill:#e6e6fa,stroke:#9370db,color:#000000
    style LMOS_Router fill:#e6e6fa,stroke:#9370db,color:#000000
    style LO fill:#e6e6fa,stroke:#9370db,color:#000000
    style Agent1 fill:#e6e6fa,stroke:#9370db,color:#000000
    style Agent2 fill:#e6e6fa,stroke:#9370db,color:#000000
    style Agent3 fill:#e6e6fa,stroke:#9370db,color:#000000
```

## Configuration

LMOS Runtime can be configured using Kubernetes ConfigMaps and Secrets.
To customize the settings, create a ConfigMap or Secret and mount it to the LMOS Runtime deployment.

You can adjust the following properties:

| Property                                        | Kubernetes ConfigMaps/ Secrets | Description                                            | Default                     |
|-------------------------------------------------|--------------------------------|--------------------------------------------------------|-----------------------------|
| `lmos.runtime.channelRoutingRepository.baseUrl` | CHANNEL_ROUTING_REPOSITORY_URL | URL of the channel routing repository                  | `http://lmos-operator:8080` |
| `lmos.runtime.openAI.url`                       | LLM_BASE_URL                   | LLM Base URL                                           | `https://api.openai.com/v1` |
| `lmos.runtime.openAI.model`                     | LLM_MODEL_NAME                 | LLM to use                                             | `gpt-3.5-turbo`             |
| `lmos.runtime.openAI.maxTokens`                 | LLM_MAX_TOKENS                 | Maximum tokens for model requests                      | `20000`                     |
| `lmos.runtime.openAI.temperature`               | LLM_TEMPERATURE                | Temperature for model requests                         | `0.0`                       |
| `lmos.runtime.openAI.format`                    | LLM_FORMAT                     | Output format for model requests                       | `json_format`               |
| `lmos.runtime.openAI.key`                       | LLM_API_KEY                    | LLM API key (**should be set as a Kubernetes secret**) | `null`                      |
| `lmos.router.classifier.vector.enabled`         | CLASSIFIER_VECTOR_ENABLED      | LLM API key (**should be set as a Kubernetes secret**) | `null`                      |

### Classifier Configuration
The [LMOS Agent Classifier library](https://github.com/eclipse-lmos/lmos-router?tab=readme-ov-file#agent-classifier) is used to identify the most appropriate agent based on the conversation and system context. Four classifier strategies can be enabled, as described below:

| Property                                           | Kubernetes ConfigMaps/ Secrets      | Description                              | Default |
|----------------------------------------------------|-------------------------------------|------------------------------------------|---------|
| `lmos.router.classifier.vector.enabled`            | CLASSIFIER_VECTOR_ENABLED           | Enables vector classification            | `false` |
| `lmos.router.classifier.llm.enabled`               | CLASSIFIER_LLM_ENABLED              | Enables LLM classification               | `true`  |
| `lmos.router.classifier.hybrid-rag.enabled`        | CLASSIFIER_HYBRID_RAG_ENABLED       | Enables Hybrid-RAG classification        | `false` |
| `lmos.router.classifier.hybrid-fast-track.enabled` | CLASSIFIER_HYBRID_FASTTRACK_ENABLED | Enables Hybrid-Fast-Track classification | `false` |

If a LLM is involved in the classification process, it must be configured accordingly.

| Property                        | Kubernetes ConfigMaps/ Secrets | Description                                                | Default                     |
|---------------------------------|--------------------------------|------------------------------------------------------------|-----------------------------|
| `lmos.router.llm.provider`      | LLM_PROVIDER                   | LLM provider (e.g. openai, azure_openai)                   | `openai`                    |
| `lmos.router.llm.base-url`      | LLM_BASE_URL                   | Base URL of the LLM provider                               | `https://api.openai.com/v1` |
| `lmos.router.llm.model`         | LLM_MODEL_NAME                 | Model to be used                                           | `gpt-3.5-turbo`             |
| `lmos.router.llm.api-key`       | LLM_API_KEY                    | API key to access the model (should be stored as a secret) | `null`                      |

If semantic classification is involved, an embedding model must be configured.

| Property                                 | Kubernetes ConfigMaps/ Secrets         | Description                                                                      | Default                                |
|------------------------------------------|----------------------------------------|----------------------------------------------------------------------------------|----------------------------------------|
| `lmos.router.embedding.model.provider`   | EMBEDDING_MODEL_PROVIDER               | Embedding model provider (openai, huggingface)                                   | `openai`                               |
| `lmos.router.embedding.model.api-key`    | EMBEDDING_MODEL_API_KEY                | API key for OpenAI or Huggingface embedding model (should be stored as a secret) | `null`                                 |
| `lmos.router.embedding.model.base-url`   | EMBEDDING_MODEL_OPENAI_BASE_URL        | OpenAI Base URL, if OpenAI embedding model is used                               | `https://api.openai.com/v1/embeddings` |
| `lmos.router.embedding.model.model-name` | EMBEDDING_MODEL_HUGGINGFACE_MODEL_NAME | Huggingface model name, if Huggingface embedding model is used                   | `intfloat/multilingual-e5-large`       |

In addition to the embedding model, a vector store must be configured to persist and query the embeddings.

| Property                                 | Kubernetes ConfigMaps/ Secrets | Description                                                | Default     |
|------------------------------------------|--------------------------------|------------------------------------------------------------|-------------|
| `lmos.router.embedding.store.host`       | EMBEDDING_STORE_HOST           | Host of the embedding store                                | `localhost` |
| `lmos.router.embedding.store.port`       | EMBEDDING_STORE_PORT           | Port of the embedding store                                | `6334`      |
| `lmos.router.embedding.store.tlsEnabled` | EMBEDDING_STORE_TLS_ENABLED    | Enable TLS for embedding store                             | `false`     |
| `lmos.router.embedding.store.apiKey`     | EMBEDDING_STORE_API_KEY        | API key for embedding store (should be stored as a secret) | `null`      |



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
curl -X POST "http://127.0.0.1:<port>/lmos/runtime/apis/v1/<tenant>/chat/<conversationId>/message" \
     -H "Content-Type: application/json" \
     -H "x-turn-id: <turnId>" \
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

## Modules
### lmos-runtime-core
Core module containing data models, interfaces, and exceptions.

### lmos-runtime-spring-boot-starter
Spring Boot starter for easy integration into Spring Boot applications. Provides auto-configuration and properties management.

### lmos-runtime-service
Service module containing the main application logic, controllers, and exception handlers.

## Code of Conduct

This project has adopted the [Contributor Covenant](https://www.contributor-covenant.org/) in version 2.1 as our code of conduct. Please see the details in our [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). All contributors must abide by the code of conduct.

By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Licensing
Copyright (c) 2025 Deutsche Telekom AG and others.

Sourcecode licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0) (the "License"); you may not use this project except in compliance with the License.

This project follows the [REUSE standard for software licensing](https://reuse.software/).    
Each file contains copyright and license information, and license texts can be found in the [./LICENSES](./LICENSES) folder. For more information visit https://reuse.software/.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the LICENSE for the specific language governing permissions and limitations under the License.