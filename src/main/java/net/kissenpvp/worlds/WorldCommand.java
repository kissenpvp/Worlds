package net.kissenpvp.worlds;

import net.kissenpvp.core.api.command.CommandPayload;
import net.kissenpvp.core.api.command.annotations.ArgumentName;
import net.kissenpvp.core.api.command.annotations.CommandData;
import net.kissenpvp.pulvinar.api.command.ArgumentParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldCommand implements ArgumentParser<World> {

    private static boolean directoryInvalid(@NotNull String worldName) {
        File worldFolder = new File(Bukkit.getServer().getWorldContainer(), worldName);
        return !worldFolder.exists() || !worldFolder.isDirectory();
    }

    @CommandData(value = "world.create", description = "Creates a new world.")
    public void createWorld(@NotNull CommandPayload<CommandSender> payload, @NotNull @ArgumentName("name") String name) {
        WorldCreator newWorldCreator = new WorldCreator(name);
        payload.getSender().sendMessage(Component.translatable("command.create.starting", Component.text(name)));

        newWorldCreator.createWorld();
        payload.getSender().sendMessage(Component.translatable("world.load.successful", Component.text(name)));
    }

    @CommandData(value = "world.load", description = "Loads an exiting world.")
    public void loadWorld(@NotNull CommandPayload<CommandSender> payload, @NotNull @ArgumentName("name") String name) {
        if (directoryInvalid(name)) {
            Component directory = Component.text(name);
            payload.getSender().sendMessage(Component.translatable("command.load.invalid.directory", directory));
            return;
        }
        new WorldCreator(name).createWorld();
        Worlds.getPlugin(Worlds.class).getWorldList().add(name);
        payload.getSender().sendMessage(Component.translatable("world.load.successful", Component.text(name)));
    }

    @CommandData(value = "world.unload", description = "Unloads an exiting world.")
    public void unloadWorld(@NotNull CommandPayload<CommandSender> payload, @NotNull World world) {
        if (!world.getPlayers().isEmpty()) {
            payload.getSender().sendMessage(Component.translatable("command.players.inside"));
            return;
        }

        Bukkit.unloadWorld(world, true);
        Worlds.getPlugin(Worlds.class).getWorldList().remove(world.getName());
        payload.getSender().sendMessage(Component.translatable("command.unload.successful", Component.text(world.getName())));
    }

    @CommandData(value = "world.delete", description = "Unloads an exiting world.")
    public void deleteWorld(@NotNull CommandPayload<CommandSender> payload, @NotNull World world) {
        if (!world.getPlayers().isEmpty()) {
            payload.getSender().sendMessage(Component.translatable("command.players.inside"));
            return;
        }
        payload.getSender().sendMessage(Component.translatable("command.delete.confirmation", Component.text(world.getName())));
        payload.confirmRequest(() ->
        {
            try {
                Bukkit.unloadWorld(world, true);
                FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer(), world.getName()));
                Worlds.getPlugin(Worlds.class).getWorldList().remove(world.getName());
                payload.getSender().sendMessage(Component.translatable("command.delete.successful", Component.text(world.getName())));
            } catch (IOException ioException) {
                throw new RuntimeException(ioException); // what happened?
            }
        }).suppressMessage(true).send();
    }

    @CommandData(value = "worldtp", aliases = "wtp", description = "Takes you to or a target to a world.")
    public void createWorld(@NotNull CommandPayload<CommandSender> payload, @NotNull @ArgumentName("world") World world, @NotNull Optional<Player> player) {
        Player target = player.orElseGet(() -> {
            if (payload.getSender() instanceof Player current) {
                return current;
            }
            throw new UnsupportedOperationException();
        });

        target.teleport(world.getSpawnLocation());
        payload.getSender().sendMessage(Component.translatable("command.teleport.successful", target.displayName(), Component.text(world.getName())));
    }

    @Override
    public @NotNull String serialize(@NotNull World world) {
        return world.getName();
    }

    @Override
    public @NotNull World deserialize(@NotNull String s) {
        return Objects.requireNonNull(Bukkit.getWorld(s));
    }

    @Override
    public @NotNull Collection<String> tabCompletion(@NotNull CommandPayload<CommandSender> commandPayload) {
        Set<String> worlds = Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toSet());
        if(commandPayload.getSender() instanceof Player player)
        {
            worlds.remove(player.getWorld().getName());
        }
        return worlds;
    }
}
