⚡ LagDetector

Advanced Lag Detection & Monitoring for Java Minecraft Servers

LagDetector is an advanced server-side monitoring plugin designed to help staff identify potential sources of server lag, including redstone activity, high MSPT, TPS degradation, and suspicious lag locations.

«Monitor first. Act when necessary.

LagDetector does not automatically disable gameplay systems. It gives staff the information they need to investigate and decide what action should be taken.»

---

✨ Features

📊 Performance Monitoring

- Real-time TPS monitoring
- MSPT monitoring
- Lag severity detection
- Detection snapshots
- Performance history
- Configurable thresholds

🔴 Redstone Detection

- Detect potential redstone-related lag
- Locate suspicious redstone activity
- Identify the affected world
- Display exact coordinates
- Find nearby players
- Monitor without disabling redstone
- Designed to avoid interfering with large farms

🔎 Lag Scanner

Run a server scan to investigate loaded areas:

/lag scan

The scanner provides progress information while searching for potential lag sources.

Stop an active scan:

/lag scan stop

👮 Staff Detection Panel

When lag is detected, staff can receive information such as:

╔══════════════════════════════════╗
║          ⚡ LAG DETECTOR          ║
╠══════════════════════════════════╣
║ LAG TYPE:    REDSTONE            ║
║ WORLD:       world               ║
║ COORDS:      123 64 -45          ║
║ PLAYER:      PlayerName          ║
║ LAST ONLINE: Online              ║
║ PLAY TIME:   2h 15m              ║
╠══════════════════════════════════╣
║ ACTION                           ║
║ [TP] [BAN] [EXPLODE] [BREAK]     ║
╚══════════════════════════════════╝

All messages can be customized through the configuration.

---

🛠️ Staff Actions

LagDetector provides several optional administrative actions.

Action| Description
TP| Teleport staff to the detected location
BREAK| Remove the detected lag core
EXPLODE| Create an explosion at the detected location
BAN| Execute the configured ban command

Dangerous actions can require confirmation before execution.

Example:

/lag confirm

Cancel an action:

/lag cancel

---

🛡️ Monitor-Only Philosophy

LagDetector is designed around a monitor-only detection system.

By default, the detector does not attempt to:

- ❌ Disable redstone
- ❌ Limit redstone
- ❌ Disable farms
- ❌ Remove machines automatically
- ❌ Modify player gameplay
- ❌ Apply artificial redstone restrictions

Instead:

Server
  │
  ▼
Performance Monitoring
  │
  ▼
Lag Detection
  │
  ▼
Location & Player Information
  │
  ▼
Staff Notification
  │
  ▼
Staff Decision

This approach allows large farms and machines to continue operating while staff investigate actual performance problems.

---

📍 Detection Information

Each detection can contain:

Lag Type
World
X / Y / Z
Player
Last Online
Play Time
TPS
MSPT
Detection Time

This information is stored in a detection snapshot and can be used by the GUI, history system, notifications, and administrative actions.

---

🔔 Notifications

LagDetector supports both staff and console notifications.

Staff

Staff members with the appropriate permission can receive lag alerts directly in chat.

Console

The console can receive periodic notifications containing:

Lag type
World
Coordinates
Player
TPS
MSPT
Detection information

The notification interval can be configured.

Example:

notifications:
  console:
    enabled: true
    interval-seconds: 60

Supported interval examples:

10 seconds
20 seconds
30 seconds
60 seconds

---

📜 Lag History

Lag detections can be recorded for later investigation.

View history with:

/lag history

History can be used to identify:

- Frequently problematic locations
- Repeated redstone lag
- Recurring performance problems
- Patterns over time

---

🖥️ Commands

Command| Description
"/lag"| Open the main LagDetector interface
"/lag gui"| Open the staff GUI
"/lag scan"| Start a lag scan
"/lag scan stop"| Stop the current scan
"/lag history"| View lag history
"/lag info"| Display detector information
"/lag reload"| Reload configuration
"/lag toggle enable"| Enable detection
"/lag toggle disable"| Disable detection
"/lag confirm"| Confirm a pending action
"/lag cancel"| Cancel a pending action

---

🔐 Permissions

Permission| Description| Default
"lagdetector.use"| Basic LagDetector access| OP
"lagdetector.admin"| Full administrative access| OP
"lagdetector.scan"| Run lag scans| OP
"lagdetector.history"| View lag history| OP
"lagdetector.notify"| Receive lag notifications| OP
"lagdetector.action.tp"| Teleport to detections| OP
"lagdetector.action.break"| Break detected cores| OP
"lagdetector.action.explode"| Explode detected locations| OP
"lagdetector.action.ban"| Ban associated players| OP

---

⚙️ Configuration

Main configuration:

plugins/LagDetector/config.yml

Major configuration sections:

scan:
notifications:
actions:
messages:

Example:

notifications:
  console:
    enabled: true
    interval-seconds: 60
    stack-trace: true
    stack-lines: 8

Messages, scan settings, notification behavior, action settings, and other plugin behavior can be customized through the configuration.

---

🪧 Protection Sign

When configured, LagDetector can create a sign at a location where a detected lag core has been removed.

Example:

┌──────────────────┐
│ PROTECT BY       │
│ ANTI LAG         │
│ MACHINES         │
│                  │
└──────────────────┘

Sign text and behavior are configurable.

---

🚀 Performance

LagDetector is designed with server performance in mind.

The plugin avoids intentionally loading unloaded chunks during location and player operations.

The detector should still be configured appropriately for the size of the server.

Recommended workflow:

Start with conservative settings
          ↓
Monitor TPS / MSPT
          ↓
Run detection
          ↓
Review results
          ↓
Increase scan scope if necessary

For large production servers, avoid unnecessarily aggressive scanning intervals.

---

🧩 Project Structure

LagDetector/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── me/
│       │       └── itzbrezz/
│       │           └── lagdetector/
│       │               │
│       │               ├── action/
│       │               ├── command/
│       │               ├── detection/
│       │               ├── gui/
│       │               ├── history/
│       │               ├── listener/
│       │               ├── notification/
│       │               ├── scan/
│       │               ├── tracker/
│       │               └── util/
│       │
│       └── resources/
│           ├── plugin.yml
│           └── config.yml
│
├── pom.xml
└── README.md

---

🔧 Building

LagDetector uses Maven.

Build the plugin with:

mvn clean package

The compiled JAR will be generated inside:

target/

---

📦 Installation

1. Build "LagDetector".
2. Locate the generated ".jar".
3. Place the JAR into your server's:

plugins/

4. Start the server.
5. Configure:

plugins/LagDetector/config.yml

6. Restart the server or reload the plugin.

---

☕ Platform

LagDetector is intended for:

- ✅ Java Edition servers
- ✅ Bukkit-based servers
- ✅ Spigot-based servers
- ✅ Paper-based servers

Bedrock Edition is not a target platform for this project.

Always verify the exact Minecraft/server version against the API version configured in "pom.xml".

---

🧪 Recommended Testing

Before deploying to a production server:

1. Install on a test server
2. Verify plugin startup
3. Check console for errors
4. Test /lag
5. Test /lag scan
6. Test detection notifications
7. Test GUI actions
8. Test history
9. Verify permissions
10. Monitor TPS/MSPT

---

⚠️ Administrative Safety

Some actions can directly affect gameplay.

In particular:

BAN
EXPLODE
BREAK

These permissions should only be given to trusted staff.

For production servers, it is recommended to enable confirmation for destructive actions.

---

📋 Roadmap

Potential future improvements include:

- [ ] Advanced lag source ranking
- [ ] More detection modules
- [ ] Improved redstone analysis
- [ ] Historical performance graphs
- [ ] Automatic detection reports
- [ ] More GUI customization
- [ ] Per-world detection settings
- [ ] Advanced staff alerts
- [ ] Detection statistics
- [ ] Improved large-server optimization

---

📄 License

Copyright © 2026 itzbrezz.

License information may be added by the project owner.

---

<div align="center">⚡ LagDetector

Detect the problem. Locate the source. Let staff decide.

Built for Java Minecraft servers.

</div>
