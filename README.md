# ShotData Plugin

## Overview

The ShotData plugin is a Spigot/Paper plugin that sends player and server status data to the ShotDevs website API. It is designed to be lightweight, asynchronous, and configurable, with a focus on privacy and performance.

## Features

- **Player Data:** Sends player information on join and quit events.
- **Server Data:** Periodically sends server snapshots (TPS, memory, CPU, etc.).
- **Asynchronous:** All HTTP requests are sent asynchronously to avoid blocking the main server thread.
- **Retry Logic:** Implements exponential backoff for failed requests.
- **Local Caching:** Caches failed requests to a local folder and retries them later.
- **Privacy Controls:** Allows you to disable IP address collection and anonymize player names.
- **Configurable:** All features can be configured in the `config.yml` file.

## Installation

1.  **Get an API Key:**
    *   Go to the [ShotDevs website](https://shotdevs.live) and create an account.
    *   Navigate to your dashboard and generate a new plugin API key.

2.  **Install the Plugin:**
    *   Download the latest release of the ShotData plugin from the releases page.
    *   Place the `ShotData-1.0.0.jar` file in your server's `plugins` folder.

3.  **Configure the Plugin:**
    *   Start and then stop your server to generate the default `config.yml` file in `plugins/ShotData`.
    *   Open `plugins/ShotData/config.yml` and paste your API key into the `api-key` field.
    *   Review and adjust the other configuration options as needed.

4.  **Start Your Server:**
    *   Start your server. The plugin will automatically start sending data to ShotDevs.

## Usage

### Commands

-   `/shotdata status`: Shows the current status of the plugin.
-   `/shotdata sendnow`: Manually triggers a server data send. (Requires `shotdata.admin` permission)
-   `/shotdata reload`: Reloads the plugin's configuration. (Requires `shotdata.admin` permission)

### Permissions

-   `shotdata.admin`: Allows usage of the `sendnow` and `reload` subcommands.

## Building from Source

1.  Clone the repository: `git clone https://github.com/your-repo/ShotData.git`
2.  Navigate to the project directory: `cd ShotData`
3.  Build with Maven: `mvn clean package`
4.  The compiled JAR file will be in the `target` directory.

## Example Payloads

### player_data.json

```json
{
  "uuid": "00000000-0000-0000-0000-000000000000",
  "name": "PlayerName",
  "displayName": "PlayerName",
  "playerListName": "PlayerName",
  "address": "127.0.0.1",
  "isOnline": true,
  "firstPlayed": 1678886400000,
  "lastPlayed": 1678886400000,
  "hasPlayedBefore": true
}
```

### server_data.json

```json
{
  "tps": {
    "oneMinute": 20.0,
    "fiveMinutes": 20.0,
    "fifteenMinutes": 20.0
  },
  "averageTickTime": 50.0,
  "memory": {
    "total": 1073741824,
    "used": 536870912,
    "free": 536870912
  },
  "uptime": 3600000,
  "cpuLoad": 0.5,
  "players": {
    "online": 10,
    "max": 100,
    "totalRegistered": 1000
  }
}
```
