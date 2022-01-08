package mike.waypoints.mixin;

import mike.waypoints.client.Waypoint;
import mike.waypoints.client.WaypointsClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "init")
    public void onDeath(CallbackInfo ci) {
        WaypointsClient.INSTANCE.loader.lastDeathWaypoint = new Waypoint("death",
                WaypointsClient.INSTANCE.client.world.getRegistryKey().getValue().toString(),
                WaypointsClient.INSTANCE.client.player.getBlockPos());
    }
}
