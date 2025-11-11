# ShotData Website Development Wiki

This document provides all the necessary information for creating a website to display data collected by the ShotData Minecraft plugin.

## API Endpoints

The plugin sends data to two primary endpoints on your web server. You will need to create a web application that can receive `POST` requests with a JSON payload to these endpoints.

-   `POST /player_data`: Receives player-specific data when a player logs out of the server.
-   `POST /server_data`: Receives server-wide data at a regular interval defined in the plugin's `config.yml`.

Your web server should be configured to listen for these `POST` requests, parse the JSON body, and store the data in a database for display on a website. All requests from the plugin will include an `x-api-key` header containing the API key configured in the plugin's `config.yml`. You should validate this API key to ensure the data is coming from your plugin.

---

## Player Data Payload (`/player_data`)

This payload contains detailed information about a player's session and their overall statistics.

**Example Payload:**

```json
{
  "uuid": "00000000-0000-0000-0000-000000000000",
  "name": "PlayerName",
  "displayName": "PlayerName",
  "playerListName": "PlayerName",
  "address": "127.0.0.1",
  "isOnline": false,
  "firstPlayed": 1678886400000,
  "lastPlayed": 1678886400000,
  "hasPlayedBefore": true,
  "health": 20.0,
  "foodLevel": 20,
  "level": 10,
  "exp": 0.5,
  "totalExperience": 150,
  "gameMode": "SURVIVAL",
  "location": {
    "world": "world",
    "x": 100.5,
    "y": 64.0,
    "z": -200.2,
    "yaw": 90.0,
    "pitch": 0.0
  },
  "statistics": {
    "custom": {
        "jump": 150,
        "play_one_minute": 72000,
        "leave_game": 10,
        "walk_one_cm": 100000,
        "deaths": 5
    },
    "mined": {
        "cobblestone": 100,
        "iron_ore": 20
    },
    "killed": {
        "zombie": 10,
        "skeleton": 5
    }
  }
}
```

### Player Data Fields

| Field             | Type    | Description                                                                                             |
| ----------------- | ------- | ------------------------------------------------------------------------------------------------------- |
| `uuid`            | String  | The player's unique identifier.                                                                         |
| `name`            | String  | The player's username. Can be anonymized based on plugin config.                                        |
| `displayName`     | String  | The player's display name. Can be anonymized.                                                           |
| `playerListName`  | String  | The player's name in the player list. Can be anonymized.                                                |
| `address`         | String  | The player's IP address. Only collected if enabled in the config.                                       |
| `isOnline`        | Boolean | Whether the player is currently online. Will be `false` for this payload.                               |
| `firstPlayed`     | Long    | The timestamp (in milliseconds) when the player first joined the server.                                |
| `lastPlayed`      | Long    | The timestamp (in milliseconds) when the player last played. For this payload, it's the logout time.    |
| `hasPlayedBefore` | Boolean | Whether the player has played on the server before.                                                     |
| `health`          | Double  | The player's health level (out of 20).                                                                  |
| `foodLevel`       | Integer | The player's food level (out of 20).                                                                    |
| `level`           | Integer | The player's experience level.                                                                          |
| `exp`             | Float   | The player's progress to the next experience level (0.0 to 1.0).                                        |
| `totalExperience` | Integer | The player's total accumulated experience points.                                                       |
| `gameMode`        | String  | The player's game mode (e.g., `SURVIVAL`, `CREATIVE`).                                                  |
| `location`        | Object  | A map containing the player's last known location.                                                      |
| `location.world`  | String  | The name of the world the player was in.                                                                |
| `location.x`      | Double  | The X coordinate.                                                                                       |
| `location.y`      | Double  | The Y coordinate.                                                                                       |
| `location.z`      | Double  | The Z coordinate.                                                                                       |
| `location.yaw`    | Double  | The yaw (horizontal rotation).                                                                          |
| `location.pitch`  | Double  | The pitch (vertical rotation).                                                                          |
| `statistics`      | Object  | A map containing all player statistics read from their stats file. The keys are the stat names (e.g., `jump`, `walk_one_cm`, `mined.cobblestone`). The values are the corresponding stat values. |

---

## Server Data Payload (`/server_data`)

This payload contains general information about the server's status and performance.

**Example Payload:**

```json
{
  "tps": {
    "oneMinute": 20.0,
    "fiveMinutes": 19.9,
    "fifteenMinutes": 20.0
  },
  "averageTickTime": 45.5,
  "memory": {
    "total": 8192,
    "used": 4096,
    "free": 4096
  },
  "uptime": 7200000,
  "cpuLoad": 0.15,
  "players": {
    "online": 10,
    "max": 100,
    "totalRegistered": 500
  }
}
```

### Server Data Fields

| Field                   | Type    | Description                                                              |
| ----------------------- | ------- | ------------------------------------------------------------------------ |
| `tps`                   | Object  | A map containing the server's Ticks Per Second over different intervals. |
| `tps.oneMinute`         | Double  | Average TPS over the last minute.                                        |
| `tps.fiveMinutes`       | Double  | Average TPS over the last 5 minutes.                                     |
| `tps.fifteenMinutes`    | Double  | Average TPS over the last 15 minutes.                                    |
| `averageTickTime`       | Double  | The average time in milliseconds for a single server tick.               |
| `memory`                | Object  | A map containing memory usage information in megabytes (MB).             |
| `memory.total`          | Long    | Total memory allocated to the server.                                    |
| `memory.used`           | Long    | Memory currently in use by the server.                                   |
| `memory.free`           | Long    | Free memory available to the server.                                     |
| `uptime`                | Long    | The server's uptime in milliseconds.                                     |
| `cpuLoad`               | Double  | The recent CPU usage of the server process (0.0 to 1.0).                 |
| `players`               | Object  | A map containing player count information.                               |
| `players.online`        | Integer | The number of players currently online.                                  |
| `players.max`           | Integer | The maximum number of players the server can hold.                       |
| `players.totalRegistered` | Integer | The total number of unique players who have ever joined the server.      |

---