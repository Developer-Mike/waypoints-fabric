package mike.waypoints.mixin;

import mike.waypoints.client.Waypoint;
import mike.waypoints.client.WaypointsClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class PlayerDeathMixin {
    @Inject(at = @At("HEAD"), method = "onDeathMessage")
    public void onDeath(DeathMessageS2CPacket packet, CallbackInfo ci) {
        if (WaypointsClient.INSTANCE.client.world == null) return;

        Entity entity = WaypointsClient.INSTANCE.client.world.getEntityById(packet.getEntityId());

        if (entity == null) return;
        if (entity != WaypointsClient.INSTANCE.client.player) return;

        WaypointsClient.INSTANCE.loader.lastDeathWaypoint = new Waypoint("death",
                WaypointsClient.INSTANCE.client.world.getRegistryKey().getValue().toString(),
                WaypointsClient.INSTANCE.client.player.getBlockPos());
    }
}
