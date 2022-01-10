# waypoints-fabric
A simple waypoint system for fabric. (In development)

Commands:
  - `pos`
    - `all`: List all waypoints
    - `add <name> [<pos>]`: Add new waypoint
    - `get <name>`: Get waypoint position
    - `remove <name>`: Remove waypoint
    - `nav`
      - `start <name>`: Start navigation to waypoint
      - `stop`: Stop navigation
    - `death`
      - `save`: Save last death position
      - `nav`: Save last death position and start navigating to it
