package mike.waypoints.mixin;

import com.mojang.bridge.launcher.SessionEventListener;
import mike.waypoints.client.Waypoint;
import mike.waypoints.client.WaypointsClient;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class WaypointNavigator  implements SessionEventListener {
    final int navigationStringLength = 20;
    final String navigationStringPlaceholder = "-";

    @Inject(at =  @At("HEAD"), method = "tick")
    public void onTick(CallbackInfo ci) {
        Waypoint waypoint = WaypointsClient.INSTANCE.loader.navigatingWaypoint;

        if (waypoint == null) return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        player.sendMessage(GetDirectionString(player, waypoint), true);
    }

    Text GetDirectionString(PlayerEntity player, Waypoint waypoint) {
        double angleToWaypoint = 180.0 / Math.PI * Math.atan2(
                player.getBlockPos().getX() - waypoint.pos.getX(),
                player.getBlockPos().getZ() - waypoint.pos.getZ()
        );

        double playerAngle = player.getRotationClient().y + 180;
        double angle = angleToWaypoint + playerAngle;
        boolean isPositive = angle >= 0;
        while (angle > 180 || angle < -180) {
            angle -= isPositive ? 360 : -360;
        }
        angle += 45;
        if (angle < 0 || angle > 90) {
            LiteralText text = new LiteralText(navigationStringPlaceholder.repeat(navigationStringLength + 3));
            return text.formatted(Formatting.RED);
        }

        int navigationIndicatorPosition = navigationStringLength - (int) ((angle / 90) * navigationStringLength);

        Vec3d posOffset = new Vec3d(waypoint.pos.getX() - player.getBlockPos().getX(), 0, waypoint.pos.getZ() - player.getBlockPos().getZ());
        int distance = (int) Math.sqrt(posOffset.x * posOffset.x + posOffset.z * posOffset.z);

        LiteralText text = new LiteralText(navigationStringPlaceholder.repeat(navigationIndicatorPosition) + " " + distance + "m " + navigationStringPlaceholder.repeat(navigationStringLength - navigationIndicatorPosition));
        return text.formatted(Formatting.RED);
    }
}
