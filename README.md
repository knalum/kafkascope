# KafkaScope 🔍

KafkaScope is a versatile desktop application for administration, monitoring, and working with [Apache Kafka](https://kafka.apache.org/).

![App screenshot](https://raw.githubusercontent.com/knalum/kafkascope/web/app.png)

## Features

- **Browse Topics:** View all topics, partitions, and their metadata in your Kafka cluster.
- **Produce & Consume:** Send and receive messages directly from the UI.
- **Topic Management:** Create, delete, and describe topics.
- **JMX Metrics:** Monitor broker and topic metrics via JMX.
- **Configurable:** Easily configure broker connections and settings.

## Getting Started

### Prerequisites
- Java 17 or newer
- Apache Kafka cluster (local or remote)

### Build

```
mvn clean install
```

### Run

```
java -jar target/kafkascope-1.0-SNAPSHOT.jar
```

## Usage

- **Connect to Kafka:** Enter your broker URL in the settings and connect.
- **Browse Topics:** Use the left tree to explore topics and partitions.
- **Produce Messages:** Use the producer panel to send messages to a topic.
- **Consume Messages:** View consumed messages in the main table.

## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

---

## Commons Clause Restriction

The Software is provided under the Apache License 2.0, with the following Commons Clause restriction:

The Software, including any modifications or derivative works, may not be used in connection with a commercial offering or service, except with explicit, prior written permission from the copyright holder.

For more information, see https://commonsclause.com/.

## Acknowledgements

- Built with Java Swing
- Uses Apache Kafka client libraries

---

For questions or contributions, please open an issue or pull request.

Created by Konstantin Nalum
