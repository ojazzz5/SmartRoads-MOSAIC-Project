# Smart Roads — ITS & C-ITS Simulation
### Eclipse MOSAIC + SUMO | Tangenziale di Napoli

A multi-scenario traffic simulation of Intelligent Transportation Systems (ITS)
and Cooperative ITS (C-ITS / V2X) on a real road network in Naples, Italy.

Built using **Eclipse MOSAIC** co-simulation framework and **SUMO** traffic simulator
as part of the MSc Autonomous Vehicle Engineering — University of Naples.

---

## 🗺️ Road Network

**Location:** Tangenziale di Napoli (Naples ring road), Italy
**Coordinates:** 40.8562°N, 14.2205°E
**Simulation duration:** 3600 seconds (1 hour)

---

## 🚦 Three Scenarios

| Scenario | Technology | Description |
|---|---|---|
| **S0 — No ITS** | Baseline | Normal traffic, no V2X communication |
| **S1 — ITS** | V2I (Vehicle-to-Infrastructure) | TMC broadcasts VMS → 40% of drivers reroute |
| **S2 — C-ITS** | V2X (Vehicle-to-Everything) | Vehicles broadcast incident warnings to each other |

---

## 📊 Results

### Time Loss Comparison — All Scenarios
![Time Loss Comparison](docs/TimeLoss_Comparison_All.png)

> C-ITS (S2) outperforms both the ITS (S1) and No-ITS (S0) baseline,
> demonstrating the quantitative benefit of cooperative V2X communication.

---

## 🧠 Application Code (Java)

### S1 — ITS Vehicle App (`ItsVehicleApp.java`)
- Polls for VMS (Variable Message Sign) status every 1 second
- 40% compliance rate — realistic driver behavior modeling
- Compliant vehicles calculate and switch to a new route via MOSAIC NavigationModule

### S1 — ITS TMC App (`ItsTmcApp.java`)
- Traffic Management Center activates VMS when incident detected
- Sets `ItsGlobals.VMS_ACTIVE = true` to trigger vehicle rerouting

### S2 — C-ITS Vehicle App (`VehicleCitsApp.java`)
- Enables **ad-hoc V2X radio** on DSRC CCH (Control Channel)
- Detects when speed drops below 5 m/s (congestion/incident)
- Broadcasts `INCIDENT_WARNING` V2X message to 500m radius
- Other vehicles receive warning and **automatically reroute** — no infrastructure needed

### S2 — C-ITS RSU App (`RsuCitsApp.java`)
- Road Side Unit application for cooperative relay of warnings

---

## 🛠️ Tools & Technologies

| Tool | Role |
|---|---|
| **Eclipse MOSAIC** | Co-simulation orchestration framework |
| **SUMO** | Urban traffic simulation |
| **Java (Maven)** | Application logic (V2X, ITS, routing) |
| **DSRC / ITS-G5** | V2X wireless communication standard |
| **JSON** | Scenario configuration |

---

## 📁 File Structure

```
├── S0_NoITS/               # Baseline: no communication
│   ├── SUMO/               # Road network (.net.xml, routes)
│   ├── mapping/            # Vehicle/RSU unit mapping
│   └── scenario_config.json
├── S1_ITS/                 # V2I: TMC + VMS rerouting
│   ├── src/main/java/
│   │   ├── ItsGlobals.java     # Global VMS state
│   │   ├── ItsTmcApp.java      # Traffic Management Center
│   │   └── ItsVehicleApp.java  # Vehicle VMS response (40% compliance)
│   └── pom.xml             # Maven build config
├── S2_CITS/               # V2X: cooperative incident warning
│   ├── src/main/java/
│   │   ├── VehicleCitsApp.java # V2V broadcast + reroute
│   │   └── RsuCitsApp.java     # RSU cooperative relay
│   └── pom.xml
├── docs/
│   └── TimeLoss_Comparison_All.png
├── run_S0.bat             # Run baseline simulation
├── run_S1.bat             # Run ITS simulation
├── run_S2.bat             # Run C-ITS simulation
└── scenario_config.json
```

## ⚙️ Prerequisites

- [Eclipse MOSAIC](https://eclipse.dev/mosaic/) installed
- [SUMO](https://sumo.dlr.de/) installed
- Java JDK 17+
- Maven

Update the paths in `run_S0/S1/S2.bat` to match your local installation.

## 🔗 Related Projects

- [AEB System](https://github.com/ojazzz5/AEB-Simulink-Project) — Automatic Emergency Braking
- [ACC System](https://github.com/ojazzz5/ACC-SIMULINK-PROJECT) — Adaptive Cruise Control
- [LKAS](https://github.com/ojazzz5/LKAS-Simulink-Project) — Lane Keeping Assist System
- [LCS](https://github.com/ojazzz5/LCS-Simulink-Project) — Lane Changing System

## 👤 Author

Ojas Srivastava — MSc Autonomous Vehicle Engineering, University of Naples