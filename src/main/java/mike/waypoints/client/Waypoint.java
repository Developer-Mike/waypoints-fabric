package mike.waypoints.client;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Waypoint {
    public String name;
    public String worldName;
    public BlockPos pos;

    public Waypoint(String name, String worldName, BlockPos pos) {
        this.name = name;
        this.worldName = worldName;
        this.pos = pos;
    }

    //Saved string = Name;worldName;X,Y,Z
    public Waypoint(String storageString) {
        String[] values = storageString.split(";");

        this.name = values[0];
        this.worldName = values[1];

        String[] coordinates = values[2].split(",");
        this.pos = new BlockPos(new Vec3i(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2])));
    }

    @Override
    public String toString() {
        return name + ";" + worldName + ";" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
