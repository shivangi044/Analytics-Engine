# Analytics Engine: Event-Driven Price Processing

> A production-grade, high-throughput analytics system built to process real-time price updates at scale — with fault isolation, full observability, and zero message loss.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-231F20?style=flat-square&logo=apachekafka)](https://kafka.apache.org/)
[![Prometheus](https://img.shields.io/badge/Prometheus-monitored-E6522C?style=flat-square&logo=prometheus&logoColor=white)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Grafana-dashboarded-F46800?style=flat-square&logo=grafana&logoColor=white)](https://grafana.com/)
[![Docker](https://img.shields.io/badge/Docker-containerized-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

---

## The Problem This Solves

Price data is volatile and arrives in bursts. A naive synchronous pipeline fails under load and collapses entirely on malformed input. This system solves both problems: it decouples ingestion from processing via Kafka, and uses a Dead Letter Queue (DLQ) strategy to isolate bad messages so a single corrupt payload can never halt the entire consumer pipeline.

---

## Architecture

```
┌─────────────┐     HTTP POST      ┌──────────────────┐     Kafka Topic      ┌──────────────────┐
│  Client /   │ ─────────────────► │   REST Ingestion  │ ───────────────────► │  Kafka Consumer  │
│  External   │                    │      Layer        │                      │  (Spring Boot)   │
│   System    │                    └──────────────────┘                      └────────┬─────────┘
└─────────────┘                                                                       │
                                                                          ┌───────────┴────────────┐
                                                                          │                        │
                                                                   ✅ Valid Msg           ❌ Poison Pill
                                                                   (processed)          (routed to DLQ)
                                                                          │                        │
                                                              ┌───────────┘               ┌────────┘
                                                              ▼                           ▼
                                                       Downstream Logic          Dead Letter Queue
                                                                                 (isolated, no crash)

Observability: Micrometer → Prometheus → Grafana (consumer lag, throughput, JVM health)
```

**Key design decisions:**

- **Kafka as the backbone** — producers and consumers scale independently; no tight coupling between ingestion and processing
- **KRaft mode** — eliminates ZooKeeper dependency, simplifying ops and reducing failure surface area
- **`ErrorHandlingDeserializer`** — wraps deserialization so malformed messages are caught at the framework level, not in your business logic
- **DLQ** — bad messages are parked for later inspection, not silently dropped or re-queued forever

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Framework | Spring Boot 4.x | Production-ready defaults, tight Kafka integration |
| Messaging | Apache Kafka (KRaft) | Durable, replayable, horizontally scalable |
| Metrics | Micrometer | Vendor-neutral instrumentation; one dependency, multiple backends |
| Monitoring | Prometheus + Grafana | Industry-standard pull-based metrics + custom dashboards |
| Infrastructure | Docker Compose | Single-command local environment with all dependencies |

---

## Observability

The system exposes a Prometheus-compatible metrics endpoint at `/actuator/prometheus`. A custom Grafana dashboard tracks the signals that matter in production:

**Consumer Lag** — the leading indicator of a bottleneck. When lag grows, you know processing can't keep up with ingestion — before users notice.

**Throughput (msg/sec)** — real-time view of pipeline activity. Useful for capacity planning and spotting sudden drops during deployments.

**JVM Health** — heap usage, GC pressure, and thread pool saturation. Because infrastructure problems often look like application problems until you look deeper.

> The core lesson: observable systems recover faster. Knowing *what* is broken is half the fix.

---

## Getting Started

**Prerequisites:** Docker and Docker Compose installed.

```bash
# Clone the repository
git clone https://github.com/your-username/analyticsEngine.git
cd analyticsEngine

# Start Kafka, Prometheus, and Grafana
docker-compose up -d

# Send a sample price update
curl -X POST http://localhost:8080/api/prices \
  -H "Content-Type: application/json" \
  -d '{"symbol": "AAPL", "price": 189.45, "timestamp": "2025-01-15T10:30:00Z"}'
```

**Access the monitoring stack:**

| Service | URL | Default Credentials |
|---|---|---|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | — |
| Actuator metrics | http://localhost:8080/actuator/prometheus | — |

---

## Key Engineering Concepts Demonstrated

**Fault Tolerance via DLQ**
The `ErrorHandlingDeserializer` intercepts deserialization failures before they reach consumer logic. Malformed messages are routed to a dedicated dead-letter topic where they can be inspected, replayed, or discarded — without ever stopping the main pipeline.

**Eventual Consistency**
Price updates are processed asynchronously. The system is designed around the understanding that a consumer processes events *after* they occur, not *during* — which requires thinking carefully about ordering guarantees, idempotency, and state management.

**Production Observability**
Instrumentation is not an afterthought. Every consumer method tracks processing time and outcome via Micrometer, giving the Grafana dashboard real signal rather than synthetic heartbeats.

---

## What I'd Add Next

- **Schema Registry** (Confluent / AWS Glue) — enforce Avro/Protobuf contracts at the broker level to catch producer/consumer schema drift before it reaches the DLQ
- **Consumer group rebalance alerting** — surface partition reassignment events in Grafana to distinguish "slow consumer" from "rebalancing consumer"
- **Idempotent consumers** — deduplication logic using Redis or a DB-backed seen-message store, enabling safe at-least-once delivery semantics
- **Load testing harness** — k6 or Gatling scripts to characterize max throughput and lag thresholds under simulated burst traffic

---

## License

MIT
