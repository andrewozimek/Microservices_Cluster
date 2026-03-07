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

## How To Compile

From the root project directory:

    javac -d out src/controller/*.java src/common/*.java src/service_nodes/*.java src/client/*.java

---

## How To Run

Open 3 separate terminals and run in this order:

Terminal 1 — Start the Server:

    java -cp out controller.Server

Terminal 2 — Start the Service Nodes:

    java -cp out service_nodes.ServiceNodeRunner

Terminal 3 — Start the Client:

    java -cp out client.Client

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
| Server (client-facing)   | TCP      | 5050 |
| Server (heartbeat)       | UDP      | 5051 |
| Base64EncodeDecode SN    | TCP      | 6001 |
| CompressionService SN    | TCP      | 6002 |
| CSVStatsService SN       | TCP      | 6003 |
| FileEntropyAnalyzer SN   | TCP      | 6004 |
| ImageTransform SN        | TCP      | 6005 |

---

## Fault Tolerance

- Node failure: If a SN goes down, the server detects it after 120 seconds and stops advertising that service
- Node recovery: When a SN restarts, it immediately sends a heartbeat and the server re-adds it to the available pool
- Multiple clients: The server spawns a new thread per client connection, handling requests in parallel
- Multiple requests per SN: Each SN spawns a new thread per incoming request

---

## Project Structure

src/
├── client/
│   └── Client.java
├── common/
│   └── Message.java
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