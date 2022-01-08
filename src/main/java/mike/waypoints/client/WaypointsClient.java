package mike.waypoints.client;

import com.mojang.bridge.game.GameSession;
import com.mojang.bridge.launcher.SessionEventListener;
import com.mojang.brigadier.CommandDispatcher;
import mike.waypoints.client.commands.WaypointCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.MinecraftClientGame;

@Environment(EnvType.CLIENT)
public class WaypointsClient implements ClientModInitializer {
    public static WaypointsClient INSTANCE;

    public MinecraftClient client;
    public MinecraftClientGame game;
    public WaypointsLoader loader;

    public WaypointsClient() {
        INSTANCE = this;
    }

    @Override
    public void onInitializeClient() {
        client = MinecraftClient.getInstance();
        game = client.getGame();
        new WaypointCommands();

        WaypointsClient.INSTANCE.game.setSessionEventListener(new SessionEventListener() {
            @Override
            public void onStartGameSession(GameSession session) {
                loader = new WaypointsLoader(session);
            }
        });
    }
}
