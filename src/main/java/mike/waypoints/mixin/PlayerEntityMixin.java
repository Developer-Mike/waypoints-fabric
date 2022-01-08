package mike.waypoints.mixin;

import mike.waypoints.client.Waypoint;
import mike.waypoints.client.WaypointsClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(at = @At(value = "HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        System.out.println("Death");

        WaypointsClient.INSTANCE.loader.lastDeathWaypoint = new Waypoint("death",
                WaypointsClient.INSTANCE.client.world.getRegistryKey().getValue().toString(),
                WaypointsClient.INSTANCE.client.player.getBlockPos());
    }
}
