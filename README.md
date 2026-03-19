# QU Microservices Cluster

A distributed networking system built for CSC340 that enables clients to submit computational tasks to a dynamic pool of worker nodes without prior knowledge of their locations.

---

## System Architecture

The system consists of three entities:
- **Server** (controller/router) — central hub clients connect to
- **Client** (task requester) — submits tasks and receives results
- **Service Nodes (SNs)** (task performers) — process tasks and return results

**TCP** is used for reliable task assignment, data transfer, and result delivery.
**UDP** is used for lightweight heartbeats and failure detection.

---

## Services Implemented

| Service | Description |
|---------|-------------|
| Base64EncodeDecode | Encodes and decodes text using Base64 |
| CompressionService | Compresses and decompresses text using GZIP |
| CSVStatsService | Computes mean, median, std deviation, min, max |
| FileEntropyAnalyzer | Calculates Shannon entropy of Base64-encoded file data |
| ImageTransform | Resize, thumbnail, rotate, and grayscale images |

---

## Configuration

The system uses a `config.properties` file for all network configuration, eliminating hardcoded IP addresses and port numbers.

**config.properties location:** Root directory (`config.properties`)
**Classpath requirement:** Include current directory (`.`) in classpath at runtime

**Default config.properties:**
```properties
# Server Configuration
server.host=127.0.0.1
server.tcp.port=5050
server.udp.port=5051

# Service Node Port Mappings
service.base64.port=6001
service.compression.port=6002
service.csv.port=6003
service.entropy.port=6004
service.image.port=6005
```

### ConfigLoader Utility Class

A new `src/config/ConfigLoader.java` utility loads configuration at runtime with these methods:

- `ConfigLoader.getServerHost()` — Returns server hostname/IP
- `ConfigLoader.getServerTcpPort()` — Returns TCP port
- `ConfigLoader.getServerUdpPort()` — Returns UDP port
- `ConfigLoader.getServicePort(String serviceName)` — Returns port for specific service
- `ConfigLoader.reload()` — Reload configuration (for testing)

**Features:**
- Automatic fallback to default values if config.properties is missing
- Port number validation with error handling
- Thread-safe static initialization
- Comprehensive warning logs

### Modified Source Files

**Server.java** — Loads TCP/UDP ports from ConfigLoader instead of hardcoded constants

**Client.java** — Dynamically loads server host and TCP port from ConfigLoader

**UDPListener.java** — Updated constructor to accept UDP port as parameter

**ServiceNode.java** — Uses ConfigLoader for server configuration defaults (CLI args still override)

**ServiceNodeRunner.java** — All service ports now loaded from ConfigLoader

### Configuration Examples

**Local Development (Default):**
```properties
server.host=127.0.0.1
server.tcp.port=5050
server.udp.port=5051
service.base64.port=6001
service.compression.port=6002
service.csv.port=6003
service.entropy.port=6004
service.image.port=6005
```

**Remote Server:**
```properties
server.host=192.168.1.100
server.tcp.port=5050
server.udp.port=5051
service.base64.port=6001
service.compression.port=6002
service.csv.port=6003
service.entropy.port=6004
service.image.port=6005
```

---

## Quick Start

### Before You Start: Get the Server IP Address

**On the server computer:**
1. Open Command Prompt or Terminal
2. Run: `ipconfig` (Windows) or `ifconfig` (Linux/Mac)
3. Look for "IPv4 Address" — this is your server's IP (e.g., `192.168.1.100`)

### Step 1: Compile the Code

```sh
javac -d out src/config/*.java src/controller/*.java src/service_nodes/*.java src/client/*.java
```

### Step 2: Update config.properties on ALL Machines

On **every** computer (server, nodes, client), edit `config.properties` and set the server IP:

```properties
server.host=192.168.1.100
server.tcp.port=5050
server.udp.port=5051
service.base64.port=6001
service.compression.port=6002
service.csv.port=6003
service.entropy.port=6004
service.image.port=6005
```

Replace `192.168.1.100` with the actual server IP you found from `ipconfig`.

### Step 3: Run the Server

**On the server computer:**
```sh
java -cp "out;." controller.Server
```

### Step 4: Run Service Nodes

**On separate computers or terminals**, run each service:

```sh
java -cp "out;." service_nodes.ServiceNodeRunner base64
java -cp "out;." service_nodes.ServiceNodeRunner compression
java -cp "out;." service_nodes.ServiceNodeRunner csv
java -cp "out;." service_nodes.ServiceNodeRunner entropy
java -cp "out;." service_nodes.ServiceNodeRunner image
```

### Step 5: Run the Client

**On a client computer:**
```sh
java -cp "out;." client.Client
```

---

## Important Notes

- **ipconfig**: Run this on the server machine FIRST to get its IP address
- **config.properties**: Must have the same `server.host` value on all machines
- **Ports**: Make sure ports 5050, 5051, and 6001-6005 are not blocked by your firewall
- **Same Network**: All machines must be connected to the same WiFi or network

---

## Configuration Examples (Reference)

**Custom Ports:**
```properties
server.host=localhost
server.tcp.port=9000
server.udp.port=9001
service.base64.port=9100
service.compression.port=9101
service.csv.port=9102
service.entropy.port=9103
service.image.port=9104
```

---

## How To Compile

From the root project directory:

    javac -d out src/config/*.java src/controller/*.java src/service_nodes/*.java src/client/*.java

---

## How To Run

Open 3 separate terminals and run in this order:

**Terminal 1 — Start the Server:**

    java -cp "out;." controller.Server

**Terminal 2 — Start the Service Nodes:**

    java -cp "out;." service_nodes.ServiceNodeRunner base64
    java -cp "out;." service_nodes.ServiceNodeRunner compression
    java -cp "out;." service_nodes.ServiceNodeRunner csv
    java -cp "out;." service_nodes.ServiceNodeRunner entropy
    java -cp "out;." service_nodes.ServiceNodeRunner image

**Terminal 3 — Start the Client:**

    java -cp "out;." client.Client

*Note: The classpath includes `.` (current directory) so that `config.properties` is accessible at runtime. On PowerShell, the classpath must be in quotes: `"out;."`*

---

## How To Use The Client

1. The client connects to the server and displays a list of available services
2. Enter the number of the service you want
3. Follow the prompts to enter your input
4. The result is displayed

Example Inputs Per Service:

Base64EncodeDecode
    Command: ENCODE
    Input text: hello
    Result: OK|aGVsbG8=

CompressionService
    Command: COMPRESS
    Input text: hello world
    Result: OK|<base64 encoded gzip data>

CSVStatsService
    Enter numbers: 10,20,30,40,50
    Result: OK|Mean: 30.00, Median: 30.00, StdDev: 14.14, Min: 10.00, Max: 50.00

FileEntropyAnalyzer
    Enter Base64-encoded file data: aGVsbG8=
    Result: OK|Entropy: 2.xxxx bits/byte | Low entropy - plain text

ImageTransform
    Command: GRAYSCALE
    Base64 image: <base64 encoded image>
    Result: OK|<base64 encoded grayscale image>

---

## Protocol Design

Heartbeat Protocol (UDP)
Each SN sends a heartbeat to the server every 15-30 seconds (random interval):
    HEARTBEAT:<serviceName>:<tcpPort>

The server tracks the last heartbeat time per SN. If no heartbeat is received for 120 seconds, the SN is marked as dead and removed from the available services list. When a SN recovers and sends a heartbeat again, it is automatically re-added.

Client-Server Protocol (TCP)
Server to Client (service list):
    SERVICES:Base64EncodeDecode,CompressionService,CSVStatsService,...

Client to Server (task request):
    ServiceName|COMMAND|data

Server to Client (result):
    OK|<result>
    ERROR|<message>

Server-SN Protocol (TCP)
Server to SN (forwarded payload):
    COMMAND|data

SN to Server (result):
    OK|<result>
    ERROR|<message>

---

## Ports Used

| Component                | Protocol | Port |
|--------------------------|----------|------|
| Server (client-facing)   | TCP      | 5050* |
| Server (heartbeat)       | UDP      | 5051* |
| Base64EncodeDecode SN    | TCP      | 6001* |
| CompressionService SN    | TCP      | 6002* |
| CSVStatsService SN       | TCP      | 6003* |
| FileEntropyAnalyzer SN   | TCP      | 6004* |
| ImageTransform SN        | TCP      | 6005* |

*Ports are now configurable in `config.properties`

---

## Fault Tolerance

- Node failure: If a SN goes down, the server detects it after 120 seconds and stops advertising that service
- Node recovery: When a SN restarts, it immediately sends a heartbeat and the server re-adds it to the available pool
- Multiple clients: The server spawns a new thread per client connection, handling requests in parallel
- Multiple requests per SN: Each SN spawns a new thread per incoming request

---

## Key Benefits of Configuration-Based Approach

1. **No Recompilation Required** — Change ports/host by editing config.properties
2. **Environment-Specific Configuration** — Different configs for dev, test, production
3. **Centralized Management** — Single file to manage all network settings
4. **Backward Compatible** — Command-line arguments still override config values
5. **Fallback Defaults** — Application works with default settings if config is missing
6. **Runtime Flexibility** — Server host can be changed dynamically for service nodes

---

## Troubleshooting Configuration

**Issue:** "WARNING: config.properties file not found in classpath"
- **Solution:** Ensure config.properties is in the root directory and classpath includes `.` current directory. Check execution command includes `;.` in classpath.

**Issue:** "WARNING: Invalid server.tcp.port value"
- **Solution:** Check config.properties for non-integer port values. Invalid values default to hardcoded defaults (5050, 5051, 6001-6005).

**Issue:** Service nodes can't connect to controller
- **Solution:** Verify `server.host`, `server.tcp.port`, and `server.udp.port` values in config.properties match your network setup.

**Issue:** Service nodes fail to start
- **Solution:** Check that config.properties contains all required service ports (base64, compression, csv, entropy, image). Run ConfigLoader.reload() if configuration was changed at runtime.

---

## Migration Notes

- **Classpath requirement:** Both compilation and execution require `src/config/` to be compiled
- **Default values:** ConfigLoader defaults match original hardcoded values (127.0.0.1, ports 5050-5051, 6001-6005)
- **CLI argument support:** ServiceNode still accepts command-line arguments for serverHost and serverUdpPort
- **Backward compatible:** Existing shell scripts using command-line args continue to work

---

```
src/
├── config/
│   └── ConfigLoader.java          (Configuration utility)
├── client/
│   └── Client.java
├── controller/
│   ├── Server.java
│   ├── ClientHandler.java
│   └── UDPListener.java
└── service_nodes/
    ├── ServiceNode.java
    ├── ServiceNodeRunner.java
    ├── Base64EncodeDecode.java
    ├── CompressionService.java
    ├── CSVStatsService.java
    ├── FileEntropyAnalyzer.java
    └── ImageTransform.java

config.properties                  (Configuration file at root)
```