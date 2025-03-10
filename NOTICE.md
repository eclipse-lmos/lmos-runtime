# NOTICE: Docker Images for LMOS Sample Agents

This repository provides Docker images for the LMOS Sample Agents, enabling easy deployment in LMOS environments. Below, you will find details on available images, how to use them, and how to build them locally if needed.

## Available Docker Images

The following Docker images are available for the sample agents:

| Agent Name       | Docker Image Name                         | Description                                  |
|------------------|------------------------------------------|----------------------------------------------|
| **Sample Agent 1**  | `eclipselmos/sample-agent-1:latest`      | A basic agent for demonstration purposes.   |
| **Sample Agent 2**  | `eclipselmos/sample-agent-2:latest`      | A more advanced sample agent.               |

## Pulling Docker Images

To pull the Docker images from the registry, use the following command:

```sh
 # Replace <image-name> with the desired image
docker pull eclipselmos/<image-name>:latest
```
Example:

```sh
docker pull eclipselmos/sample-agent-1:latest
```

## Running Docker Containers

To run an agent as a Docker container, execute:

```sh
docker run -d --name sample-agent-1 eclipselmos/sample-agent-1:latest
```
Modify the command based on the agent you wish to run. You can also pass environment variables if needed:

```sh
docker run -d -e ENV_VAR=value --name sample-agent-1 eclipselmos/sample-agent-1:latest
```

## Building Docker Images Locally

If you prefer to build the images locally, use the provided Dockerfile:

# Navigate to the agent directory

```sh
cd sample-agent-1
```

# Build the Docker image

```sh
docker build -t sample-agent-1:latest .
```

A## dditional Information

- Ensure you have Docker installed and running before executing the above commands.
- Refer to the repository documentation for further details on configuring and using the agents.

For any issues or inquiries, please open an issue in this repository.