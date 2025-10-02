<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->
# Contributing to Intelligent Agent Routing System

We welcome contributions to the Intelligent Agent Routing System! Whether you're fixing bugs, adding new features, improving documentation, or providing feedback, your help is appreciated. Please follow the guidelines below to ensure a smooth contribution process.

## Table of Contents

1. [How to Contribute](#how-to-contribute)
2. [Getting Started](#getting-started)
3. [Development Workflow](#development-workflow)
4. [Commit Messages](#commit-messages)
5. [Pull Request Guidelines](#pull-request-guidelines)
6. [Style Guide](#style-guide)
7. [Reporting Issues](#reporting-issues)

## How to Contribute

### Reporting Bugs

If you find a bug, please report it by creating an issue in the [issue tracker](https://github.com/eclipse-lmos/lmos-router/issues). Include as much detail as possible to help us diagnose and fix the problem quickly.

### Suggesting Enhancements

If you have an idea for a new feature or an improvement, please open an issue in the [issue tracker](https://github.com/eclipse-lmos/lmos-router/issues) to discuss it before starting any work. This helps us coordinate efforts and avoid duplicate work.

### Submitting Pull Requests

1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Make your changes.
4. Ensure all tests pass.
5. Submit a pull request.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Gradle
- Git

### Setup

1. **Clone the repository**:
    ```bash
    git clone https://github.com/eclipse-lmos/lmos-runtime.git
    cd lmos-runtime
    ```

2. **Build the project**:
    ```bash
    ./gradlew build
    ```

## Development Workflow

1. **Create a branch**:
    ```bash
    git checkout -b feature/your-feature-name
    ```

2. **Make changes**: Implement your feature or bugfix.

3. **Run tests**: Ensure all tests pass.
    ```bash
    ./gradlew test
    ```

4. **Commit changes**: Follow the [commit message guidelines](#commit-messages).

5. **Push to your fork**:
    ```bash
    git push origin feature/your-feature-name
    ```

6. **Open a pull request**: Go to the repository on GitHub and open a pull request.

## Commit Messages

- Use the present tense ("Add feature" not "Added feature").
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...").
- Limit the first line to 72 characters or less.
- Reference issues and pull requests liberally.
- You can also follow [conventional commit messages](https://www.conventionalcommits.org/) for better readability. For example:
    - `feat`: A new feature.
    - `fix`: A bugfix.
    - `docs`: Documentation changes.
    - `style`: Code style changes.
    - `refactor`: Code refactoring.
    - `test`: Add or modify tests.
    - `chore`: Maintenance tasks.

## Pull Request Guidelines

- Ensure your pull request (PR) adheres to the project's coding standards.
- Include tests for new features or bugfixes.
- Update the documentation if necessary.
- Describe your changes in the PR description.
- Link to any relevant issues or pull requests.

## Style Guide

- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use meaningful variable and function names.
- Write clear and concise comments where necessary.

## Reporting Issues

If you encounter any issues, please report them in the [issue tracker](https://github.com/eclipse-lmos/lmos-router/issues). Provide as much detail as possible, including steps to reproduce the issue, your environment, and any relevant logs or screenshots.

Thank you for contributing to the Intelligent Agent Routing System!