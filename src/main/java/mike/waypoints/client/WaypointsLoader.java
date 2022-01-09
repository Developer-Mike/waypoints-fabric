package mike.waypoints.client;

import com.mojang.bridge.game.GameSession;
import com.mojang.bridge.launcher.SessionEventListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public class WaypointsLoader implements SessionEventListener {

    MinecraftClient client = WaypointsClient.INSTANCE.client;
    File worldWaypointsDir;
    public ArrayList<Waypoint> waypoints = new ArrayList<>();
    public Waypoint lastDeathWaypoint = null;
    public Waypoint navigatingWaypoint = null;

    public WaypointsLoader() {
        WaypointsClient.INSTANCE.game.setSessionEventListener(this);
    }

    @Override
    public void onStartGameSession(GameSession session) {
        String worldIdentifier;
        if (session.isRemoteServer())
            worldIdentifier = client.getCurrentServerEntry().address;
        else
            worldIdentifier = client.getServer().getSavePath(WorldSavePath.ROOT).getParent().getFileName().toString();

        Path waypointsDir = FabricLoader.getInstance().getGameDir().resolve("waypoints");
        worldWaypointsDir = waypointsDir.resolve(worldIdentifier + ".wp").toFile();
        if (!worldWaypointsDir.exists()) {
            try {
                worldWaypointsDir.getParentFile().mkdirs();
                worldWaypointsDir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            waypoints = new ArrayList<>();
        } else  {
            LoadWaypoints();
        }
    }

    @Override
    public void onLeaveGameSession(GameSession session) {
        waypoints = new ArrayList<>();
        navigatingWaypoint = null;
        lastDeathWaypoint = null;
    }

    void LoadWaypoints() {
        try (BufferedReader br = new BufferedReader(new FileReader(worldWaypointsDir))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                if (line.equals("")) continue;

                waypoints.add(new Waypoint(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean AddWaypoint(Waypoint waypoint) {
        if (waypoints.stream().anyMatch(wp -> wp.name.equals(waypoint.name))) return false;

        try(FileWriter fw = new FileWriter(worldWaypointsDir, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(waypoint.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        waypoints.add(waypoint);
        return true;
    }

    public boolean RemoveWaypoint(String waypointName) {
        if (waypoints.stream().noneMatch(wp -> wp.name.equals(waypointName))) return false;

        try(FileWriter fw = new FileWriter(worldWaypointsDir, false);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            try (BufferedReader br = new BufferedReader(new FileReader(worldWaypointsDir))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // process the line.

                    if (Objects.equals(new Waypoint(line).name, waypointName)) continue;

                    out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        waypoints.remove(GetWaypoint(waypointName));
        return true;
    }

    public Waypoint GetWaypoint(String waypointName) {
        return waypoints.stream().filter(wp -> wp.name.equals(waypointName)).findFirst().orElse(null);
    }
}
