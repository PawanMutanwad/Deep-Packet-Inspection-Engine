# Deep Packet Inspection (DPI) Engine

![Java](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36.svg?style=for-the-badge&logo=apachemaven)
![Network](https://img.shields.io/badge/Network-Packet%20Inspection-blue.svg?style=for-the-badge&logo=wireshark)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)
![Build Status](https://img.shields.io/badge/Tests-12%2F12%20Passing-brightgreen.svg?style=for-the-badge)

A high-performance, multithreaded **Deep Packet Inspection (DPI) Engine** engineered in modern Java 21 from first principles. This system parses raw binary PCAP (Packet Capture) streams across the OSI network stack (L2 to L7), maintains stateful 5-tuple flow records, extracts Layer-7 application metadata (HTTPS TLS SNI, HTTP Host headers, and UDP DNS Queries), dynamically evaluates declarative JSON firewall policies, and outputs compliant traffic into a filtered PCAP capture file.

---

## ⚡ At a Glance (Executive Summary)

| Metric / Aspect | Value / Specification |
| :--- | :--- |
| **Ingestion Capacity** | Tested with 13,938+ packets (~10.5 MB sample PCAPs) |
| **Concurrency Model** | Producer-Consumer Architecture (`ArrayBlockingQueue` + `ExecutorService` Thread Pool) |
| **Protocol Decoders** | Ethernet II, IPv4, IPv6, TCP, UDP, TLS 1.2/1.3 (SNI), HTTP 1.1 (Host), DNS (RFC 1035 QNAME) |
| **Flow Management** | Stateful 5-Tuple (`ConcurrentHashMap`) with `LongAdder` atomic metric counters |
| **Rule Engine** | Declarative JSON configuration (`input/rules.json`) loaded via Jackson |
| **Verification** | 100% JUnit 5 automated test suite coverage (`mvn test`) |

---

## 🎯 Overview

Conventional packet filtering (stateless firewalls) evaluates only network and transport headers (IP addresses and port numbers). Modern network security demands **Deep Packet Inspection (DPI)**—inspecting the actual data payload to identify protocols, applications, and malicious content.

This DPI engine provides full L2–L7 visibility without relying on external packet-parsing frameworks (such as Pcap4j or Libpcap wrappers). It parses raw byte streams directly, handles endianness conversions, decodes length-prefixed protocol fields, tracks multi-packet connection states, and applies real-time security rules.

> [!NOTE]
> **Primary Use Cases**: Network Traffic Analysis, Next-Generation Firewalls (NGFW), ISP Bandwidth Management, Intrusion Detection Systems (IDS), and Protocol Audit Logging.

---

## ✨ Key Technical Features

- **🚀 Concurrent Producer-Consumer Architecture**: Segregates file/network I/O (`PcapReader`) from CPU-intensive packet inspection using a bounded `ArrayBlockingQueue` (capacity: 8,192) and a 4-worker `ExecutorService` thread pool.
- **📦 Zero-Dependency Binary PCAP Engine**: Reads and writes raw binary PCAP captures (24-byte global headers, 16-byte packet headers) via low-level Java I/O streams and endianness bit manipulation (`Integer.reverseBytes`).
- **🛡️ Full-Stack Protocol Decoders**:
  - **L2 (Ethernet II)**: MAC formatting and EtherType resolution (`0x0800` IPv4, `0x86DD` IPv6).
  - **L3 (IPv4 & IPv6)**: Variable Internet Header Length (`IHL`), TTL, Protocol type, and 40-byte fixed IPv6 base header parsing.
  - **L4 (TCP & UDP)**: Port extraction, Sequence/Ack tracking, and TCP/UDP payload offset calculations.
  - **L7 (Application)**: HTTPS TLS ClientHello SNI extension extraction (RFC 6066), HTTP Host header parsing (RFC 7230), and UDP DNS Query QNAME label decoding (RFC 1035).
- **🔁 Stateful 5-Tuple Connection Tracking**: Aggregates packets into network flows using a `ConcurrentHashMap` keyed by `(Source IP, Destination IP, Source Port, Destination Port, Protocol)`.
- **⚙️ Dynamic JSON Rule Engine**: Declarative firewall policies loaded dynamically from `input/rules.json` via Jackson, allowing hot-reloading of IP, Application, and Domain blocklists without recompilation.
- **📊 Real-time Metric Reporting**: Thread-safe atomic counters (`LongAdder`) compile comprehensive traffic and application breakdown reports upon execution.
- **🧪 Automated Unit Testing**: 100% passing JUnit 5 test suite covering SNI extraction, HTTP parsing, DNS QNAME decoding, IPv4 header parsing, and 5-tuple equality contracts.

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

## ⚡ How It Works (Packet Inspection Flow)

```
[PcapReader Stream]
       │
       ▼
[ArrayBlockingQueue] ──► [Worker Thread (PacketProcessor)]
                                   │
                         Parse Ethernet Frame
                                   │
              ┌────────────────────┴────────────────────┐
              ▼                                         ▼
      EtherType == 0x0800                       EtherType == 0x86DD
         (IPv4Parser)                              (IPv6Parser)
              │                                         │
              └────────────────────┬────────────────────┘
                                   ▼
                         Parse Transport Header
              ┌────────────────────┴────────────────────┐
              ▼                                         ▼
        Protocol == 6                            Protocol == 17
         (TCPParser)                              (UDPParser)
              │                                         │
              ▼                                         ▼
    Extract TCP Payload                       Extract UDP Payload
    (Parse TLS SNI / HTTP)                      (Parse DNS Query)
              │                                         │
              └────────────────────┬────────────────────┘
                                   ▼
                         Construct 5-Tuple Key
                                   │
                       Lookup/Create Flow Record
                     (Concurrent ConnectionTracker)
                                   │
                        Classify Application
                      (ApplicationClassifier)
                                   │
                         Evaluate Rule Engine
                             (RuleManager)
                           /               \
                   [Allowed]               [Blocked]
                      │                        │
            Write to PcapWriter           Drop Packet
```

---

## 🧩 Key Implemented Components

| Class / Component | Module Package | Core Responsibility |
| :--- | :--- | :--- |
| **`Main`** | `com.pawan.dpi` | Application entry point; initializes queue, worker pool, and shutdown hooks. |
| **`PcapReader`** | `com.pawan.dpi.parser` | Reads PCAP global/packet headers with byte-reversal endianness translation. |
| **`PcapWriter`** | `com.pawan.dpi.service` | Thread-safe synchronized writer constructing valid PCAP binaries for allowed packets. |
| **`PacketProcessor`** | `com.pawan.dpi.service` | Core pipeline engine. Executes L2–L7 decoding, flow tracking, and rule checks. |
| **`ConnectionTracker`** | `com.pawan.dpi.tracker` | Thread-safe flow storage (`ConcurrentHashMap`) mapping 5-tuples to `Flow` states. |
| **`SNIExtractor`** | `com.pawan.dpi.parser` | Scans TCP payloads for TLS Handshake (`0x16`), ClientHello (`0x01`), and SNI (`0x0000`). |
| **`HTTPParser`** | `com.pawan.dpi.parser` | Extracts `Host:` header strings from plain-text HTTP requests. |
| **`DNSParser`** | `com.pawan.dpi.parser` | Decodes RFC 1035 length-prefixed domain labels from UDP port 53 packets. |
| **`ApplicationClassifier`** | `com.pawan.dpi.service` | Maps hostnames/domains to `ApplicationType` enums (YOUTUBE, GITHUB, GOOGLE, etc.). |
| **`RuleManager`** | `com.pawan.dpi.rules` | Parses dynamic IP, Domain, and Application blocking rules from JSON. |
| **`DPIReport`** | `com.pawan.dpi.report` | Lock-free atomic counter aggregator (`LongAdder`) compiling terminal summary metrics. |

---

## 🛠️ Engineering Challenges & Technical Solutions

### 1. Endianness & Binary Format Translation
* **Challenge**: PCAP binary files store integer headers in Little-Endian byte order on x86 architectures, whereas Java standard streams process bytes in Big-Endian.
* **Solution**: Implemented bit-reversal transformations (`Integer.reverseBytes()` and `Short.reverseBytes()`) inside `PcapReader` and bit-shifting masks (`>>>`) inside `PcapWriter` to maintain binary specification integrity.

### 2. High-Throughput Thread Safety
* **Challenge**: Concurrent worker threads updating shared flow counters and writing to output streams can lead to data races and file corruption.
* **Solution**: Employed `ConcurrentHashMap` for lock-free flow lookups, `LongAdder` primitives for high-concurrency metric increments, and `synchronized` output stream methods to guarantee atomicity.

### 3. Disambiguating Encrypted & Unencrypted L7 Protocols
* **Challenge**: Identifying application domains across mixed traffic types (HTTPS, HTTP, DNS) without incurring performance overhead.
* **Solution**: Developed modular, lightweight L7 parsers (`SNIExtractor`, `HTTPParser`, `DNSParser`) that short-circuit early if payload signatures fail to match expected protocol bytes (e.g., checking for TLS record type `0x16` or HTTP verb strings).

---

## 📂 Project Structure

```text
dpi-engine/
├── input/
│   ├── rules.json             # Dynamic JSON firewall rule configuration
│   └── sample.pcap             # Sample input PCAP binary file
├── output/
│   └── filtered.pcap           # Output PCAP destination for allowed packets
├── src/
│   ├── main/java/com/pawan/dpi/
│   │   ├── Main.java           # Multithreaded Producer-Consumer orchestrator
│   │   ├── model/
│   │   │   ├── ApplicationType.java # Supported application enum types
│   │   │   ├── EthernetFrame.java   # L2 Ethernet model
│   │   │   ├── FiveTuple.java       # Stateful 5-tuple hash key
│   │   │   ├── IPv4Packet.java      # L3 IPv4 model
│   │   │   ├── IPv6Packet.java      # L3 IPv6 model
│   │   │   ├── Packet.java          # Full raw packet wrapper
│   │   │   ├── PacketHeader.java    # 16-byte PCAP packet header model
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
│   │   │   └── PcapWriter.java          # Thread-safe PCAP binary writer
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
├── pom.xml                     # Maven build configuration
├── README.md                   # Project documentation
└── ARCHITECTURE.md             # System architecture specification
```

---

## 📥 Installation & Quickstart

### Prerequisites
- **Java Development Kit (JDK) 21** or higher
- **Apache Maven 3.9.0** or higher
- **Git**

### Build Steps

```bash
# 1. Clone the repository
git clone https://github.com/PawanMutanwad/Deep-Packet-Inspection-Engine.git

# 2. Change to project directory
cd Deep-Packet-Inspection-Engine/dpi-engine

# 3. Clean and compile project
mvn clean compile
```

---

## 🚀 Execution & Verification

### 1. Run Automated Unit Tests

```bash
mvn test
```

> [!TIP]
> Executes 12 comprehensive unit test cases verifying TLS SNI extraction, HTTP Host parsing, DNS label decoding, IPv4 header parsing, and 5-tuple equality contracts.

### 2. Execute the DPI Engine

```bash
mvn exec:java -Dexec.mainClass="com.pawan.dpi.Main"
```

---

## 📊 Verification & Execution Output

Terminal output upon analyzing a PCAP capture containing 13,938 packets:

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

> [!IMPORTANT]
> The filtered file `output/filtered.pcap` contains exactly the **13,427 allowed packets** and can be loaded directly into **Wireshark** for payload inspection.

---

## 💼 Technical Resume Highlights

For technical recruiters and hiring managers, this project highlights key competency areas:

- **Core Java & Computer Systems**: Low-level binary stream parsing, bitwise mask/shift operations, endianness conversion, RFC compliance.
- **Multithreading & Concurrency**: Producer-Consumer design, worker pools (`ExecutorService`), atomic state counters (`LongAdder`), lock-free maps (`ConcurrentHashMap`).
- **Networking Depth**: Full OSI stack understanding (L2 Ethernet, L3 IP, L4 TCP/UDP, L7 TLS/HTTP/DNS), 5-tuple flow aggregation.
- **Production Readiness**: Dynamic JSON rule management, clean OOP architecture, 100% passing automated test suite (JUnit 5).

---

## 📜 License

Distributed under the **MIT License**. See `LICENSE` for details.

---

## 👤 Author

**Pawan Mutanwad**
- **GitHub**: [@PawanMutanwad](https://github.com/PawanMutanwad)
- **Repository**: [Deep-Packet-Inspection-Engine](https://github.com/PawanMutanwad/Deep-Packet-Inspection-Engine)
