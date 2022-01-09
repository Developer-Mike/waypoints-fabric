package mike.waypoints.mixin;

import com.mojang.bridge.launcher.SessionEventListener;
import mike.waypoints.client.Waypoint;
import mike.waypoints.client.WaypointsClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class WaypointNavigator  implements SessionEventListener {
    final int navigationStringLength = 20;
    final String navigationStringIndicator = "|||";
    final String navigationStringPlaceholder = "-";

    @Inject(at =  @At("HEAD"), method = "tick")
    public void onTick(CallbackInfo ci) {
        Waypoint waypoint = WaypointsClient.INSTANCE.loader.navigatingWaypoint;

        if (waypoint == null) return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        player.sendMessage(GetDirectionString(player, waypoint), true);
    }

    Text GetDirectionString(PlayerEntity player, Waypoint waypoint) {
        int navigationIndicatorPosition = navigationStringLength / 2;

        LiteralText text = new LiteralText(navigationStringPlaceholder.repeat(navigationIndicatorPosition) + navigationStringIndicator + navigationStringPlaceholder.repeat(navigationStringLength - navigationIndicatorPosition));
        return text.formatted(Formatting.RED);
    }
}
