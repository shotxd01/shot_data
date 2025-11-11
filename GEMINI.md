# Project Overview

This project is a Spigot/Paper plugin for Minecraft servers that collects and sends player and server data to the ShotDevs web service. It is written in Java and uses Maven for dependency management and building.

The plugin gathers a variety of data points, including:
- **Player Data:** Join/quit events, player information (name, UUID, IP address), health, experience, location, and various in-game statistics (blocks placed/broken, damage dealt, etc.).
- **Server Data:** Ticks per second (TPS), memory usage, CPU load, uptime, and player counts.

Key features include:
- Asynchronous data transmission to avoid impacting server performance.
- A retry mechanism with exponential backoff for failed API requests.
- Local caching of failed requests to prevent data loss.
- Configurable privacy options to anonymize player names and disable IP address collection.

## Building and Running

### Building from Source

To build the plugin from source, you will need Java 11+ and Maven installed.

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    ```
2.  **Navigate to the project directory:**
    ```bash
    cd shotdata
    ```
3.  **Build with Maven:**
    ```bash
    mvn clean package
    ```
The compiled JAR file will be located in the `target/` directory.

### Running the Plugin

1.  Place the compiled JAR file into the `plugins/` directory of your Spigot or Paper Minecraft server.
2.  Start the server to generate the default `config.yml` file.
3.  Edit the `config.yml` file to add your API key and configure the desired settings.
4.  Restart the server.

## Development Conventions

- The project follows standard Java conventions.
- Asynchronous operations are used for all network requests to prevent blocking the main server thread.
- The plugin uses a `config.yml` file for configuration, with options for API settings, data collection, privacy, and logging.
- Player and server data is sent to the ShotDevs API as JSON payloads.
- Failed requests are cached locally in the `plugins/shot_data/pending` directory and retried later.
- The plugin includes a command (`/shotdata`) for checking the plugin's status, manually sending data, and reloading the configuration.
