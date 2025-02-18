# Adventure Builder

## All things adventure...

A tool to configure adventure games. Currently, this is text only.

## Getting Started

1. Clone the repository
2. Start a MongoDb instance as defined in `Dockerfile.mongodb.yaml`
3. Run `mvn spring-boot:run`
4. Open your browser to `http://localhost:8080`
5. Start configuring your adventure!

## Features

This project is still under development. But here are some of the features that are currently available:

1. Create a new adventure
2. Define the vocabulary
3. Add rooms
4. Define the room connections (exits)

There is also a simple game engine that can be used to play your game. You can find it in 
the `AdventureClient` class.
