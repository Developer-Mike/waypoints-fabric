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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import javax.swing.text.AttributeSet;
import javax.swing.text.Style;

public class WaypointCommands {
    final String commandPrefix = "wp";

    public WaypointCommands() {
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal(commandPrefix).executes(null)
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
            SendWaypoint(player, waypoint);
        } else {
            SendErrorMessage(player, "Waypoint already exists.");

            return 0;
        }

        return 1;
    }

    private int removeCommand(CommandContext<FabricClientCommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ClientPlayerEntity player)) return -1;

        String waypointName = ctx.getArgument("name", String.class);

        boolean success = WaypointsClient.INSTANCE.loader.RemoveWaypoint(waypointName);
        if (success) {
            SendSuccessMessage(player, "Removed waypoint.");
        } else {
            SendErrorMessage(player, "No such Waypoint.");

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
            SendErrorMessage(player, "No Waypoint with this name found.");

            return -1;
        }

        return 1;
    }

    private int saveDeathCommand(CommandContext<FabricClientCommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ClientPlayerEntity player)) return -1;

        Waypoint waypoint = WaypointsClient.INSTANCE.loader.lastDeathWaypoint;
        if (waypoint != null) {
            WaypointsClient.INSTANCE.loader.RemoveWaypoint(waypoint.name);
            WaypointsClient.INSTANCE.loader.AddWaypoint(waypoint);

            SendWaypoint(player, waypoint);
        } else {
            SendErrorMessage(player, "You have no unsaved Death.");

            return -1;
        }

        return 1;
    }

    void SendSuccessMessage(ClientPlayerEntity player, String message) {
        player.sendMessage(new LiteralText(message).formatted(Formatting.GREEN), false);
    }

    void SendErrorMessage(ClientPlayerEntity player, String message) {
        player.sendMessage(new LiteralText(message).formatted(Formatting.RED), false);
    }

    void SendWaypoint(ClientPlayerEntity player, Waypoint waypoint) {
        MutableText nameText = new LiteralText(waypoint.name).formatted(Formatting.BLUE);
        MutableText positionText = new LiteralText("[" + waypoint.pos.getX() + ", " + waypoint.pos.getY() + ", " + waypoint.pos.getZ() + "]").formatted(Formatting.YELLOW)
                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wp navigate " + waypoint.name)));
        MutableText worldNameText = new LiteralText(waypoint.worldName.split(":")[1]).formatted(Formatting.GREEN);
        MutableText separator = new LiteralText(" | ");

        player.sendMessage(nameText.append(separator).append(positionText).append(separator).append(worldNameText), false);
    }
}
