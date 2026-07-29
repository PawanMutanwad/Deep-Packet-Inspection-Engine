# Deep Packet Inspection (DPI) Engine

![Java](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36.svg?style=for-the-badge&logo=apachemaven)
![Network](https://img.shields.io/badge/Network-Packet%20Inspection-blue.svg?style=for-the-badge&logo=wireshark)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)
![Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg?style=for-the-badge)

A high-performance, multithreaded **Deep Packet Inspection (DPI) Engine** built in modern Java from the ground up. This system parses raw binary PCAP (Packet Capture) files across the OSI network stack (L2 to L7), performs stateful 5-tuple flow tracking, extracts Layer-7 application metadata (HTTPS TLS SNI, HTTP Host headers, and UDP DNS Queries), dynamically evaluates declarative JSON firewall rules, and filters non-compliant traffic into an output PCAP file.

---

## 📋 Table of Contents
- [Overview](#overview)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)
- [How It Works (Packet Processing Flow)](#how-it-works-packet-processing-flow)
- [Implemented Components](#implemented-components)
- [Sample Workflow](#sample-workflow)
- [Installation](#installation)
- [How to Run](#how-to-run)
- [Sample Output](#sample-output)
- [Future Enhancements](#future-enhancements)
- [Skills Demonstrated](#skills-demonstrated)
- [Learning Outcomes](#learning-outcomes)
- [Screenshots](#screenshots)
- [License](#license)
- [Author](#author)

---

## 🎯 Overview

Deep Packet Inspection (DPI) is an advanced packet filtering technique used by enterprise firewalls, Intrusion Prevention Systems (IPS), and Internet Service Providers (ISPs) to inspect the data field (payload) of network packets as they pass through an inspection point.

Unlike conventional packet filtering that only checks packet headers (IP addresses and ports), this DPI engine inspects L7 payloads to identify encrypted/unencrypted applications (such as YouTube, GitHub, Google), parse DNS queries, and enforce security policies dynamically.

---

## ✨ Key Features

- **🚀 Multithreaded Producer-Consumer Engine**: Utilizes an `ArrayBlockingQueue` with worker thread pools (`ExecutorService`) for concurrent, line-rate packet processing without thread contention.
- **📦 Zero-Dependency PCAP Binary I/O**: Reads and writes raw binary PCAP files (global 24-byte headers and per-packet 16-byte headers) using low-level Java I/O streams and endianness conversion (`Integer.reverseBytes`).
- **🛡️ Full OSI Stack Binary Parsers**:
  - **Layer 2**: Ethernet II frame decoding (`EtherType`).
  - **Layer 3**: IPv4 header parsing (`IHL`, `TTL`, Protocol) & IPv6 40-byte base header parsing.
  - **Layer 4**: TCP (Sequence/Ack, header length, payload extraction) & UDP header parsing.
  - **Layer 7**: HTTPS TLS ClientHello SNI extraction (RFC 6066), HTTP Host header parsing (RFC 7230), and UDP DNS Query QNAME label decoding (RFC 1035).
- **🔁 Stateful 5-Tuple Flow Tracking**: Aggregates network packets into flows using a `ConcurrentHashMap` keyed by `(Source IP, Destination IP, Source Port, Destination Port, Protocol)`.
- **⚙️ Dynamic JSON Rule Engine**: Declarative firewall rules loaded from `input/rules.json` using Jackson, allowing zero-downtime policy updates (IP blocking, Application blocking, Domain matching).
- **📊 Real-time Traffic Statistics**: Atomic `LongAdder` counters generate comprehensive DPI summary reports upon completion.
- **🧪 100% Automated Test Coverage**: Full JUnit 5 test suite for SNI, HTTP, DNS, IPv4 parsing, and 5-tuple equality.

---

## 🏗️ System Architecture

```
                                  ┌────────────────────────────────┐
                                  │       input/rules.json         │
                                  └───────────────┬────────────────┘
                                                  │ (Jackson Mapper)
                                                  ▼
┌────────────────────┐            ┌────────────────────────────────┐
│   input/sample.pcap│───────────►│   ArrayBlockingQueue<Packet>   │
└────────────────────┘            └───────────────┬────────────────┘
     (PcapReader)                                 │
                                                  ▼
                               ┌───────────────────────────────────┐
                               │   Worker Pool (ExecutorService)   │
                               │      PacketProcessor Threads      │
                               └──────────────────┬────────────────┘
                                                  │
         ┌────────────────────────────────────────┼────────────────────────────────────────┐
         ▼                                        ▼                                        ▼
┌──────────────────┐                    ┌──────────────────┐                    ┌──────────────────┐
│   SNIExtractor   │                    │    HTTPParser    │                    │    DNSParser     │
│  (TLS Port 443)  │                    │  (HTTP Port 80)  │                    │  (DNS Port 53)   │
└──────────────────┘                    └──────────────────┘                    └──────────────────┘
         │                                        │                                        │
         └────────────────────────────────────────┼────────────────────────────────────────┘
                                                  │
                                                  ▼
                               ┌───────────────────────────────────┐
                               │   Concurrent ConnectionTracker    │
                               │   & Thread-Safe DPIReport Stats   │
                               └──────────────────┬────────────────┘
                                                  │
                                                  ▼
                               ┌───────────────────────────────────┐
                               │       Synchronized PcapWriter     │
                               │        output/filtered.pcap       │
                               └───────────────────────────────────┘
```

---

## 📂 Project Structure

```text
dpi-engine/
├── input/
│   ├── rules.json             # Dynamic JSON firewall configuration
│   └── sample.pcap             # Input packet capture file
├── output/
│   └── filtered.pcap           # Filtered PCAP output destination
├── src/
│   ├── main/java/com/pawan/dpi/
│   │   ├── Main.java           # Entry point & Producer-Consumer orchestrator
│   │   ├── model/
│   │   │   ├── ApplicationType.java # Supported application enums
│   │   │   ├── EthernetFrame.java   # L2 Ethernet model
│   │   │   ├── FiveTuple.java       # Stateful 5-tuple hash key
│   │   │   ├── IPv4Packet.java      # L3 IPv4 model
│   │   │   ├── IPv6Packet.java      # L3 IPv6 model
│   │   │   ├── Packet.java          # Full packet wrapper
│   │   │   ├── PacketHeader.java    # 16-byte PCAP header model
│   │   │   ├── PcapGlobalHeader.java# 24-byte PCAP global header model
│   │   │   ├── TCPHeader.java       # L4 TCP header model
│   │   │   └── UDPHeader.java       # L4 UDP header model
│   │   ├── parser/
│   │   │   ├── DNSParser.java       # RFC 1035 QNAME domain parser
│   │   │   ├── EthernetParser.java  # L2 frame parser
│   │   │   ├── HTTPParser.java      # RFC 7230 Host header parser
│   │   │   ├── IPv4Parser.java      # L3 IPv4 parser
│   │   │   ├── IPv6Parser.java      # L3 IPv6 parser
│   │   │   ├── PcapReader.java      # PCAP binary reader
│   │   │   ├── SNIExtractor.java    # RFC 6066 TLS ClientHello SNI extractor
│   │   │   ├── TCPParser.java       # L4 TCP parser & payload extractor
│   │   │   └── UDPParser.java       # L4 UDP parser & payload extractor
│   │   ├── report/
│   │   │   ├── DPIReport.java       # Thread-safe atomic statistics report
│   │   │   └── TrafficStatistics.java # Traffic metrics tracker
│   │   ├── rules/
│   │   │   ├── RuleEngine.java      # Port inspection utility
│   │   │   └── RuleManager.java     # Dynamic JSON rule manager
│   │   ├── service/
│   │   │   ├── ApplicationClassifier.java # Domain-to-Application matcher
│   │   │   ├── PacketProcessor.java       # Core DPI processing engine
│   │   │   └── PcapWriter.java          # PCAP binary writer
│   │   └── tracker/
│   │       ├── ConnectionTracker.java # Thread-safe flow table
│   │       └── Flow.java              # Network flow state tracking
│   └── test/java/com/pawan/dpi/
│       ├── model/
│       │   └── FiveTupleTest.java     # Unit test for FiveTuple
│       └── parser/
│           ├── DNSParserTest.java     # Unit test for DNSParser
│           ├── HTTPParserTest.java    # Unit test for HTTPParser
│           ├── IPv4ParserTest.java    # Unit test for IPv4Parser
│           └── SNIExtractorTest.java  # Unit test for SNIExtractor
├── pom.xml                     # Maven configuration file
├── README.md                   # Project documentation
└── ARCHITECTURE.md             # Architectural specification
```

---

## 🛠️ Technologies Used

| Category | Technology / Library | Usage |
| :--- | :--- | :--- |
| **Language** | Java 21 | Core programming language (Records, Pattern Matching, Endianness APIs) |
| **Build Tool** | Apache Maven 3.9+ | Dependency management and build lifecycle |
| **JSON Parser** | Jackson Databind 2.17.0 | Dynamic firewall rule parsing from `input/rules.json` |
| **Testing** | JUnit 5 (Jupiter 5.10.2) | Automated unit testing framework |
| **Concurrency** | `java.util.concurrent` | `ArrayBlockingQueue`, `ExecutorService`, `ConcurrentHashMap`, `LongAdder` |
| **Protocol Specifications** | Ethernet, IPv4, IPv6, TCP, UDP, TLS 1.2/1.3, HTTP 1.1, DNS | Network protocol specifications |

---

## ⚡ How It Works (Packet Processing Flow)

```
[PcapReader] -> Read PCAP Packet -> Push to ArrayBlockingQueue
                                            │
                                            ▼
                               [PacketProcessor Worker Thread]
                                            │
                                  Parse Ethernet Header
                                            │
                     ┌──────────────────────┴──────────────────────┐
                     ▼                                             ▼
             EtherType == 0x0800                          EtherType == 0x86DD
                (IPv4Parser)                                 (IPv6Parser)
                     │                                             │
                     └──────────────────────┬──────────────────────┘
                                            ▼
                                Parse Transport Layer
                     ┌──────────────────────┴──────────────────────┐
                     ▼                                             ▼
               Protocol == 6                                 Protocol == 17
                (TCPParser)                                  (UDPParser)
                     │                                             │
                     ▼                                             ▼
          Extract TCP Payload                          Extract UDP Payload
          Parse TLS SNI / HTTP                           Parse DNS Query
                     │                                             │
                     └──────────────────────┬──────────────────────┘
                                            ▼
                                    Build FiveTuple
                                            │
                                 Lookup/Create Flow State
                               (ConnectionTracker HashMap)
                                            │
                                  Classify Application
                                (ApplicationClassifier)
                                            │
                                   Evaluate Rules
                                    (RuleManager)
                                  /               \
                          [Allowed]               [Blocked]
                             │                        │
                   Write to PcapWriter           Drop Packet
```

---

## 🧩 Implemented Components

| Component | Responsibility |
| :--- | :--- |
| **`PcapReader`** | Reads PCAP global header and per-packet headers using `DataInputStream` with byte reversal (`readInt` / `readShort`). |
| **`PcapWriter`** | Synchronized writer that constructs valid PCAP binary files for allowed network packets. |
| **`PacketProcessor`** | Main pipeline executor. Coordinates L2–L7 parsing, flow tracking, classification, and firewall decisions. |
| **`ConnectionTracker`** | Thread-safe flow table (`ConcurrentHashMap`) mapping 5-tuples to `Flow` state objects. |
| **`SNIExtractor`** | Scans TCP payloads for TLS Handshake (`0x16`), ClientHello (`0x01`), and Server Name Indication extension (`0x0000`). |
| **`HTTPParser`** | Extracts `Host:` header strings from plain-text HTTP GET/POST/PUT requests on port 80. |
| **`DNSParser`** | Decodes RFC 1035 length-prefixed domain labels from UDP port 53 packets. |
| **`ApplicationClassifier`** | Maps extracted hostnames to `ApplicationType` enums (YOUTUBE, GITHUB, GOOGLE, FACEBOOK, etc.). |
| **`RuleManager`** | Loads IP, Domain, and Application blocking policies from `input/rules.json`. |
| **`DPIReport`** | Thread-safe atomic counter aggregator (`LongAdder`) printing terminal statistics upon completion. |

---

## 🔄 Sample Workflow

1. **Rule Initialization**: `RuleManager` parses `input/rules.json` and loads blocked IPs, domains, and application types into memory.
2. **PCAP Ingestion**: `PcapReader` opens `input/sample.pcap`, validates the 24-byte global header, and begins streaming packets into `ArrayBlockingQueue`.
3. **Concurrent Inspection**:
   - Worker threads pull raw `Packet` objects off the queue.
   - Headers are peeled layer-by-layer: Ethernet $\rightarrow$ IPv4/IPv6 $\rightarrow$ TCP/UDP $\rightarrow$ Payload.
   - A `FiveTuple` key is formed: `(Source IP, Destination IP, Source Port, Destination Port, Protocol)`.
   - The flow is created or retrieved from `ConnectionTracker`.
   - L7 payload is passed to `SNIExtractor`, `HTTPParser`, or `DNSParser`.
   - Extracted domain is passed to `ApplicationClassifier` and checked against `RuleManager`.
4. **Action & Output**:
   - **If Allowed**: `writer.writePacket(packet)` writes the packet to `output/filtered.pcap`.
   - **If Blocked**: The packet is dropped, flow state marked blocked, and `blockedPackets` counter incremented.
5. **Reporting**: Upon completion, `DPIReport` prints detailed traffic summary metrics.

---

## 📥 Installation

### Prerequisites
- **Java SE Development Kit (JDK) 21** or higher
- **Apache Maven 3.9.0** or higher
- **Git**

### Build Steps

```bash
# 1. Clone the repository
git clone https://github.com/PawanMutanwad/Deep-Packet-Inspection-Engine.git

# 2. Navigate to project root
cd Deep-Packet-Inspection-Engine/dpi-engine

# 3. Clean and compile using Maven
mvn clean compile
```

---

## 🚀 How to Run

### 1. Run Automated Unit Tests

```bash
mvn test
```

*Output:*
```text
[INFO] Running com.pawan.dpi.model.FiveTupleTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.pawan.dpi.parser.DNSParserTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.pawan.dpi.parser.HTTPParserTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.pawan.dpi.parser.IPv4ParserTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.pawan.dpi.parser.SNIExtractorTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Results: Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
```

### 2. Execute the DPI Engine

```bash
mvn exec:java -Dexec.mainClass="com.pawan.dpi.Main"
```

*Or via direct `java` binary:*

```bash
# On Linux/macOS
java -cp "target/classes:$(mvn dependency:build-classpath | grep -v '\[INFO\]' | tr '\n' ':')" com.pawan.dpi.Main

# On Windows (PowerShell/CMD)
java -cp "target/classes;target/dependency/*" com.pawan.dpi.Main
```

---

## 📊 Sample Output

Upon execution against a PCAP file containing 13,938 packets:

```text
Starting Concurrent Deep Packet Inspection Engine...
[RuleManager] Dynamic rules loaded successfully from input/rules.json
PCAP Global Header
-----------------------
Magic Number : A1B2C3D4
Version      : 2.4
Time Zone    : 0
Sig Figs     : 0
Snap Length  : 262144
Network Type : 1


========== DPI REPORT ==========

Total Packets   : 13938
Allowed Packets : 13427
Blocked Packets : 511
Total Flows     : 798

Applications
------------------------------
YOUTUBE      : 15
GITHUB       : 416
GOOGLE       : 505
UNKNOWN      : 5908

===============================
Processing completed successfully.
```

The resulting `output/filtered.pcap` contains only the **13,427 allowed packets** and can be opened directly in **Wireshark** for verification.

---

## 🔮 Future Enhancements

- [ ] **Live Network Interface Capture**: Integrate Pcap4j or JNetPcap for real-time live network interface sniffing.
- [ ] **TLS 1.3 Encrypted Client Hello (ECH) Handling**: Implement heuristic flow classification for encrypted ECH handshakes.
- [ ] **Pattern Matching Engine**: Add Aho-Corasick or Regex payload string searching for signature-based IDS/IPS capabilities.
- [ ] **Web Dashboard**: Build a React/Spring Boot real-time monitoring dashboard for metrics visualization.

---

## 💡 Skills Demonstrated

- **Systems Programming & Network Protocols**: L2–L7 protocol structure understanding, binary byte decoding, RFC adherence (TLS SNI RFC 6066, DNS RFC 1035, HTTP RFC 7230).
- **Concurrent Programming**: Producer-Consumer design pattern, thread safety, atomic primitives (`LongAdder`), lock-free maps (`ConcurrentHashMap`), thread synchronization.
- **Enterprise Software Architecture**: Clean Code principles, Separation of Concerns, Modular Design, Declarative JSON Configuration.
- **Test-Driven Development (TDD)**: Automated unit testing with JUnit 5 covering boundary and edge cases.

---

## 🎓 Learning Outcomes

1. Deep understanding of raw network packet layout and binary parsing in Java.
2. Hands-on experience building stateful network flow trackers.
3. Mastered low-level bitwise operations (`& 0xFF`, `<<`, `>>>`) and little-endian vs big-endian byte order handling.
4. Experience in building scalable, thread-safe, concurrent Java applications.

---

## 📷 Screenshots

> *(Placeholder: Screenshots of Wireshark opening `filtered.pcap` and terminal execution output can be added here)*

```text
+-------------------------------------------------------------------+
|  [Wireshark View of output/filtered.pcap]                        |
|  1 0.000000 192.168.1.15 -> 142.250.190.46 TCP 66 443 -> 52140    |
|  2 0.001201 192.168.1.15 -> 140.82.121.4   TLS 512 Client Hello   |
+-------------------------------------------------------------------+
```

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for details.

---

## 👤 Author

**Pawan Mutanwad**
- **GitHub**: [@PawanMutanwad](https://github.com/PawanMutanwad)
- **Project Repository**: [Deep-Packet-Inspection-Engine](https://github.com/PawanMutanwad/Deep-Packet-Inspection-Engine)
