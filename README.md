# The GurionRock Vacuum Robot Perception and Mapping System Simulation

## Overview
This project simulates the perception and mapping system of a vacuum robot, designed to process sensor data (camera, lidar and GPS) and build a map of its environment. The system is modular, using a message bus architecture to facilitate communication between different microservices.

## Features
- **Sensor Data Processing:** Handles input from camera,lidar and GPS sensors.
- **Data Fusion:** Integrates multiple sensor streams for robust perception.
- **Mapping:** Builds and updates a map based on sensor data.
- **Microservice Architecture:** Decoupled components communicate via a message bus.
- **Simulation Scenarios:** Includes error scenarios and full runs for testing.

## Project Structure
- `src/main/java/bgu/spl/mics/` - Core message bus and microservice framework.
- `src/main/java/bgu/spl/mics/application/` - Main application logic (extend as needed).
- `src/test/java/` - Unit and integration tests.
- `error_in_camera_input/`, `error_in_lidar_input/`, `full_run/` - Example input/output data for different simulation scenarios.
- `pom.xml` - Maven build configuration.

## Getting Started
### Prerequisites
- Java 8 or higher
- Maven 3.6+

### Build
```
mvn clean compile
```

### Run
1. Place your configuration and sensor data files in the appropriate input folders.
2. Run the  application (update the path as needed):
```
mvn exec:java -Dexec.mainClass=bgu.spl.mics.application.GurionRockRunner -Dexec.args="/path/to/configuration_file.json"
```

### Test
```
mvn test
```


## Project Flow
The following describes the typical flow of data and control in the simulation:

1. **Initialization**
   - The main application loads configuration and sensor data files.
   - Microservices (e.g., Camera, Lidar, GPS, Mapping, Data Fusion) are instantiated and registered with the MessageBus.

2. **Sensor Data Ingestion**
   - Camera, Lidar, and GPS microservices read their respective input data.
   - Each sensor microservice publishes its data to the MessageBus according to the simulation clock, triggered by `TickBroadcast` events. Data is sent only at its scheduled time, synchronized with the tick.

3. **Data Fusion & Processing**
   - A Data Fusion microservice subscribes to sensor data events.
   - It processes and combines data from multiple sensors to create a unified perception of the environment.

4. **Mapping**
   - The Mapping microservice subscribes to fused data events.
   - It updates the robot's internal map based on the processed sensor data.

5. **Error Handling & Simulation Scenarios**
   - Error scenarios (e.g., missing or corrupted sensor data) are simulated using the provided input folders.
   - Microservices handle errors by broadcasting a `CrushedBroadcast` when a failure or critical error occurs, allowing other services to react appropriately.

6. **Output Generation**
   - The final map and any relevant outputs are written to output files in the scenario folders.

### MessageBus Architecture
- All communication between microservices is handled via the MessageBus, ensuring loose coupling and modularity.
- Microservices subscribe to specific event or broadcast types and react accordingly.

---

- Modify the configuration files in the scenario folders to test different sensor input and error cases.
- Extend or implement new microservices in `src/main/java/bgu/spl/mics/` or `application/` as needed.



