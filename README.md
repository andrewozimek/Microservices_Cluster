# QU Microservices Cluster

A distributed networking system built for CSC340 that enables clients to submit computational tasks to a dynamic pool of worker nodes without prior knowledge of their locations.

---

## System Architecture

The system consists of three entities:
- **Server** (controller/router) — central hub clients connect to
- **Client** (task requester) — submits file-based tasks and receives results
- **Service Nodes (SNs)** (task performers) — process tasks and return results

**TCP** is used for reliable task assignment, data transfer, and result delivery.
**UDP** is used for lightweight heartbeats and failure detection.

---

## Services Implemented

| Service | Description |
|---------|-------------|
| Base64EncodeDecode | Encodes/decodes files using Base64 |
| CompressionService | Compresses/decompresses files using GZIP |
| CSVStatsService | Computes mean, median, std deviation, min, max on numeric columns |
| FileEntropyAnalyzer | Calculates Shannon entropy of a file |
| ImageTransform | Grayscale, flip, rotate, resize, thumbnail images |

---

## Configuration

All network settings are stored in `config.properties` — no hardcoded IP addresses or ports anywhere in the code.

**config.properties** must be placed in the `out/` directory (classpath root) so it is found at runtime.

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

Replace `server.host` with the actual IP address of the machine running the server.

**Find your server IP:**
- **Windows:** Open Command Prompt → run `ipconfig` → look for "IPv4 Address"
- **Mac/Linux:** Open Terminal → run `ifconfig` → look for `inet` under your active network adapter

### ConfigLoader

`src/config/ConfigLoader.java` loads the config at runtime:

- `ConfigLoader.getServerHost()` — server IP
- `ConfigLoader.getServerTcpPort()` — TCP port
- `ConfigLoader.getServerUdpPort()` — UDP port
- `ConfigLoader.getServicePort(String serviceName)` — port for a specific service

Falls back to default values if `config.properties` is missing.

---

## Quick Start

---

### Windows Instructions

#### Step 1: Open Terminal

Open **Command Prompt** or **PowerShell** and navigate to the project folder:
```cmd
cd C:\CSCnetworkfixedproject\Microservices_Cluster
```

#### Step 2: Compile

```cmd
javac -d out src/config/*.java src/controller/*.java src/service_nodes/*.java src/client/*.java
```

#### Step 3: Find the Server IP

On the **server machine**, open Command Prompt and run:
```cmd
ipconfig
```
Look for **IPv4 Address** under your WiFi adapter (e.g., `192.168.1.100`). Write it down.

#### Step 4: Update config.properties

Open `config.properties` in Notepad, change `server.host` to the server's IP, then save:
```properties
server.host=192.168.1.100
```

Then copy it into the `out/` folder:
```cmd
copy config.properties out\config.properties
```

Do this on **every machine** (server, service nodes, client).

#### Step 5: Create the out folder if it doesn't exist

```cmd
mkdir out
```

#### Step 6: Run the Server (on the server machine)

```cmd
java -cp out controller.Server
```

#### Step 7: Run Service Nodes (any machine on the same network)

Open a **new Command Prompt window** for each service node:
```cmd
java -cp out service_nodes.ServiceNodeRunner base64
```
```cmd
java -cp out service_nodes.ServiceNodeRunner compression
```
```cmd
java -cp out service_nodes.ServiceNodeRunner csv
```
```cmd
java -cp out service_nodes.ServiceNodeRunner entropy
```
```cmd
java -cp out service_nodes.ServiceNodeRunner image
```

#### Step 8: Run the Client

Open a **new Command Prompt window**:
```cmd
java -cp out client.Client
```

---

### Mac / Linux Instructions

#### Step 1: Open Terminal

Press **Cmd + Space**, type **Terminal**, and press Enter. Then navigate to the project folder:
```bash
cd /path/to/Microservices_Cluster
```
Replace `/path/to/` with wherever you saved the project. For example:
```bash
cd ~/Downloads/Microservices_Cluster
```

#### Step 2: Create the out folder if it doesn't exist

```bash
mkdir -p out
```

#### Step 3: Compile

```bash
javac -d out src/config/*.java src/controller/*.java src/service_nodes/*.java src/client/*.java
```

#### Step 4: Find the Server IP

On the **server machine**, open Terminal and run:
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```
The IP next to `inet` is your server's IP (e.g., `192.168.1.100`). Write it down.

#### Step 5: Update config.properties

Open `config.properties` in any text editor and change `server.host` to the server's IP:
```properties
server.host=192.168.1.100
```

Then copy it into the `out/` folder:
```bash
cp config.properties out/config.properties
```

Do this on **every machine** (server, service nodes, client).

#### Step 6: Run the Server (on the server machine)

```bash
java -cp out controller.Server
```

#### Step 7: Run Service Nodes (any machine on the same network)

Open a **new Terminal tab** (Cmd + T) for each service node:
```bash
java -cp out service_nodes.ServiceNodeRunner base64
```
```bash
java -cp out service_nodes.ServiceNodeRunner compression
```
```bash
java -cp out service_nodes.ServiceNodeRunner csv
```
```bash
java -cp out service_nodes.ServiceNodeRunner entropy
```
```bash
java -cp out service_nodes.ServiceNodeRunner image
```

#### Step 8: Run the Client

Open a **new Terminal tab** (Cmd + T):
```bash
java -cp out client.Client
```

---

## How To Use The Client

The client uses **file paths** as input instead of typing text directly in the terminal.

1. Run the client — it connects to the server and lists available services
2. Select a service by number
3. Enter the full path to your input file when prompted
4. Results are **saved to files** in the same folder as your input file

### Example Usage Per Service

**Base64EncodeDecode**
```
Command: ENCODE or DECODE
Enter file path (Windows): C:\Users\you\testfiles\Hello.txt
Enter file path (Mac):     /Users/you/testfiles/Hello.txt
Result saved to: testfiles\encoded_output.txt
```

**CompressionService**
```
Command: COMPRESS or DECOMPRESS
Enter file path (Windows): C:\Users\you\testfiles\Hello.txt
Enter file path (Mac):     /Users/you/testfiles/Hello.txt
COMPRESS  → saved to: testfiles\compressed_output.gz
DECOMPRESS → saved to: testfiles\decompressed_output.bin
```

**CSVStatsService**
```
Enter CSV file path (Windows): C:\Users\you\testfiles\data.csv
Enter CSV file path (Mac):     /Users/you/testfiles/data.csv
Result saved to: testfiles\csv_results.txt
Computes stats (count, mean, median, std dev, min, max) for all numeric columns.
If no numeric columns found, reports row count and unique values per column instead.
```

**FileEntropyAnalyzer**
```
Enter file path (Windows): C:\Users\you\testfiles\Hello.txt
Enter file path (Mac):     /Users/you/testfiles/Hello.txt
Result printed to terminal: Entropy: 3.12 bits/byte | Low entropy - plain text
```
Higher entropy = more complex/random file (plain text ~3–5, images ~7–8).

**ImageTransform**
```
Command: GRAYSCALE | FLIP | ROTATE | RESIZE | THUMBNAIL
Enter image file path (Windows): C:\Users\you\testfiles\photo.png
Enter image file path (Mac):     /Users/you/testfiles/photo.png
Result saved to: testfiles\output.png
```

---

## Protocol Design

**Heartbeat Protocol (UDP)**

Each SN sends a heartbeat to the server every 15–30 seconds:
```
HEARTBEAT:<serviceName>:<tcpPort>
```
If no heartbeat is received for 120 seconds, the SN is marked dead and removed. When it restarts, the next heartbeat re-adds it automatically.

**Client-Server Protocol (TCP)**
```
Server → Client:   SERVICES:Base64EncodeDecode,CompressionService,...
Client → Server:   ServiceName|COMMAND|<base64-encoded data>
Server → Client:   OK|<result>  or  ERROR|<message>
```

**Server-SN Protocol (TCP)**
```
Server → SN:   COMMAND|<base64-encoded data>
SN → Server:   OK|<result>  or  ERROR|<message>
```

Binary data (images, compressed files, raw bytes) is Base64-encoded before transmission so it can be sent safely over text-based sockets.

---

## Ports Used

| Component                | Protocol | Port  |
|--------------------------|----------|-------|
| Server (client-facing)   | TCP      | 5050* |
| Server (heartbeat)       | UDP      | 5051* |
| Base64EncodeDecode SN    | TCP      | 6001* |
| CompressionService SN    | TCP      | 6002* |
| CSVStatsService SN       | TCP      | 6003* |
| FileEntropyAnalyzer SN   | TCP      | 6004* |
| ImageTransform SN        | TCP      | 6005* |

*All ports configurable in `config.properties`

---

## Fault Tolerance & Concurrency

- **Node failure** — Server detects dead nodes after 120s and stops advertising them
- **Node recovery** — Restarted node sends heartbeat and is automatically re-added
- **Multiple clients** — Server spawns a new thread per client connection (backlog: 200)
- **Multiple requests per SN** — Each SN spawns a new thread per incoming request
- **Thread-safe registry** — Server uses `ConcurrentHashMap` for the alive nodes list

---

## Stress Testing

A stress test tool is included at `src/StressTest.java`. It spawns N simultaneous clients using a `CountDownLatch` to fire all requests at exactly the same time.

**Windows:**
```cmd
javac -cp out -d out src/StressTest.java
java -cp out StressTest
```

**Mac/Linux:**
```bash
javac -cp out -d out src/StressTest.java
java -cp out StressTest
```

Enter the number of concurrent clients when prompted. Results show success/fail counts and total time elapsed.

---

## Troubleshooting

**"config.properties not found"**
- Make sure `config.properties` is copied into the `out/` folder, not just the root
- Windows: `copy config.properties out\config.properties`
- Mac: `cp config.properties out/config.properties`

**"Could not connect to server / Connection refused"**
- Check that `server.host` in `config.properties` matches the server machine's actual IP
- Windows: run `ipconfig` on the server machine
- Mac: run `ifconfig` on the server machine

**"Failed to contact service node"**
- The service node for that service is not running or the heartbeat hasn't registered yet
- Start the service node and wait a few seconds for the heartbeat to arrive

**"Service node did not respond"**
- The service node may have crashed — check its terminal for errors and restart it
- Make sure you recompile AND restart the service node after any code change

**Firewall issues (Windows)**
- Open Windows Defender Firewall → Advanced Settings
- Add inbound rules for TCP ports 5050, 6001–6005 and UDP port 5051

**Firewall issues (Mac)**
- System Settings → Network → Firewall → allow Java through the firewall
- Or temporarily disable the firewall for testing

**All machines must be on the same network**
- Connect all devices to the same WiFi router
- Note: iPhone hotspot may block device-to-device connections (AP isolation)

---

## Project Structure

```
src/
├── config/
│   └── ConfigLoader.java
├── client/
│   └── Client.java
├── controller/
│   ├── Server.java
│   ├── ClientHandler.java
│   └── UDPListener.java
├── service_nodes/
│   ├── ServiceNode.java
│   ├── ServiceNodeRunner.java
│   ├── Base64EncodeDecode.java
│   ├── CompressionService.java
│   ├── CSVStatsService.java
│   ├── FileEntropyAnalyzer.java
│   └── ImageTransform.java
└── StressTest.java

out/                        (compiled .class files)
config.properties           (must also be copied into out/)
testfiles/                  (put your test files here)
```
