package mike.waypoints.client.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mike.waypoints.client.Waypoint;
import mike.waypoints.client.WaypointsClient;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.entity.attribute.DefaultAttributeContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class WaypointSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String name;

        try {
            name = context.getArgument("name", String.class);
        } catch (IllegalArgumentException e) { return builder.buildFuture(); }

        for (Waypoint waypoint : WaypointsClient.INSTANCE.loader.waypoints) {
            if (waypoint.name.toLowerCase().contains(name.toLowerCase())) {
                builder.suggest(waypoint.name);
            }
        }

        return builder.buildFuture();
    }
}
