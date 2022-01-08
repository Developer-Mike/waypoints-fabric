package mike.waypoints.client.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mike.waypoints.client.Waypoint;
import mike.waypoints.client.WaypointsClient;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class WaypointCommands {
    final String commandPrefix = "wp";

    public WaypointCommands() {
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal(commandPrefix)
                        .then(ClientCommandManager.literal("all").executes((this::getAllWaypoints)))
                        .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("name", StringArgumentType.string()).executes((ctx ->
                                            addCommand(ctx, false)
                                    ))
                                    .then(ClientCommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((ctx ->
                                                    addCommand(ctx, true)
                                            ))
                                    ))
                        )
                        .then(ClientCommandManager.literal("get")
                            .then(ClientCommandManager.argument("name", StringArgumentType.string()).executes((ctx ->
                                    getCommand(ctx)
                            )))
                        )
                        .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("name", StringArgumentType.string()).executes((ctx ->
                                    removeCommand(ctx)
                            )))
                        )
                        .then(ClientCommandManager.literal("navigate")
                            .then(ClientCommandManager.argument("name", StringArgumentType.string()).executes((ctx ->
                                    navigateCommand(ctx)
                            )))
                        )
                        .then(ClientCommandManager.literal("death").executes((ctx ->
                                    saveDeathCommand(ctx)
                            ))
                        )
        );
    }

    private int getAllWaypoints(CommandContext<FabricClientCommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ClientPlayerEntity player)) return -1;

        for (Waypoint waypoint : WaypointsClient.INSTANCE.loader.waypoints) {
            SendWaypoint(player, waypoint);
        }

        return 0;
    }

    private int addCommand(CommandContext<FabricClientCommandSource> ctx, boolean customPos) {
        if (!(ctx.getSource().getEntity() instanceof ClientPlayerEntity player)) return -1;

        String waypointName = ctx.getArgument("name", String.class);

        Waypoint waypoint = WaypointsClient.INSTANCE.loader.GetWaypoint(waypointName);
        if (waypoint == null) {
            String dimension = player.world.getRegistryKey().getValue().toString();

            BlockPos position;
            if (customPos) {
                ServerCommandSource fakeSource = new ServerCommandSource(null, player.getPos(), null, null, 0, null, null, null, null);
                position = new BlockPos(ctx.getArgument("pos", DefaultPosArgument.class).toAbsolutePos(fakeSource));
            } else {
                position = player.getBlockPos();
            }

            waypoint = new Waypoint(waypointName, dimension, position);

            WaypointsClient.INSTANCE.loader.AddWaypoint(waypoint);
        } else {
            //TODO: Exists already

            return 0;
        }

        return 1;
    }

    private int removeCommand(CommandContext<FabricClientCommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ClientPlayerEntity)) return -1;

        String waypointName = ctx.getArgument("name", String.class);

        boolean success = WaypointsClient.INSTANCE.loader.RemoveWaypoint(waypointName);
        if (success) {
            //TODO: Success delete
        } else {
            //TODO: Not exists

            return -1;
        }

        return 1;
    }

    private int getCommand(CommandContext<FabricClientCommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ClientPlayerEntity player)) return -1;

        String waypointName = ctx.getArgument("name", String.class);

        Waypoint waypoint = WaypointsClient.INSTANCE.loader.GetWaypoint(waypointName);
        if (waypoint != null) {
            SendWaypoint(player, waypoint);
        } else {
            //TODO: Error Not found

            return -1;
        }

        return 1;
    }

    private int navigateCommand(CommandContext<FabricClientCommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ClientPlayerEntity player)) return -1;

        String waypointName = ctx.getArgument("name", String.class);

        Waypoint waypoint = WaypointsClient.INSTANCE.loader.GetWaypoint(waypointName);
        if (waypoint != null) {
            WaypointsClient.INSTANCE.loader.navigatingWaypoint = waypoint;
        } else {
            //TODO: Error Not found

            return -1;
        }

        return 1;
    }

    private int saveDeathCommand(CommandContext<FabricClientCommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ClientPlayerEntity)) return -1;

        Waypoint waypoint = WaypointsClient.INSTANCE.loader.lastDeathWaypoint;
        if (waypoint != null) {
            WaypointsClient.INSTANCE.loader.RemoveWaypoint(waypoint.name);
            WaypointsClient.INSTANCE.loader.AddWaypoint(waypoint);
        } else {
            //TODO: Error no last death

            return -1;
        }

        return 1;
    }

    void SendWaypoint(ClientPlayerEntity player, Waypoint waypoint) {
        player.sendMessage(Text.of(waypoint.name + " | [" + waypoint.pos.getX() + ", " + waypoint.pos.getY() + ", " + waypoint.pos.getZ() + "] | " + waypoint.worldName.split(":")[1]), false);
    }
}
