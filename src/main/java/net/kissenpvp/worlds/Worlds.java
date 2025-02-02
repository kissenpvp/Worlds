package net.kissenpvp.worlds;

import net.kissenpvp.core.api.database.meta.Table;
import net.kissenpvp.core.api.database.meta.list.MetaList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Worlds extends JavaPlugin
{
    private MetaList<String> worldList;

    public @NotNull MetaList<String> getWorldList() {
        return worldList;
    }

    @Override public void onEnable()
    {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerCommand(this, new WorldCommand());
        pluginManager.registerParser(World.class, new WorldCommand(), this);

        pluginManager.registerTranslation("world.load.successful", new MessageFormat("The world {0} has been loaded successfully."), this);

        pluginManager.registerTranslation("command.create.starting", new MessageFormat("The world {0} will now get created."), this);

        pluginManager.registerTranslation("command.world.unknown", new MessageFormat("The world {0} seems to not exist or is not loaded, check /bmv help for help."), this);
        pluginManager.registerTranslation("command.players.inside", new MessageFormat("The world you are trying to operate still contains players. Make sure they are in another world."), this);

        pluginManager.registerTranslation("command.load.invalid.directory", new MessageFormat("No directory with the name {0} has been found."), this);
        pluginManager.registerTranslation("command.teleport.successful", new MessageFormat("{0} has successfully been taken to world {1}."), this);

        pluginManager.registerTranslation("command.unload.successful", new MessageFormat("The world {0} has been unloaded successfully."), this);

        pluginManager.registerTranslation("command.delete.confirmation", new MessageFormat("Are you sure you want to delete the world {0}. It wont be recoverable. If you are sure type /confirm otherwise type /cancel. This request will automatically expire after 30 seconds."), this);
        pluginManager.registerTranslation("command.delete.successful", new MessageFormat("The world {0} has been deleted successfully."), this);

        Table table = Bukkit.getPulvinar().getPrivateDatabase().createTable("worlds");
        worldList = table.registerMeta(this).getCollection("worlds", String.class).join();

        loadWorlds();
    }

    private void loadWorlds()
    {
        List<String> unrecognizedWorlds = getLoadedWorlds().stream().filter(world -> worldList.contains(world)).toList();
        if(!unrecognizedWorlds.isEmpty())
        {
            String warning = "The worlds %s have not been saved to the database. Adding them now...";
            getLogger().warning(String.format(warning, unrecognizedWorlds));
            worldList.addAll(unrecognizedWorlds);
        }

        worldList.forEach(world -> {
            if(Objects.isNull(Bukkit.getWorld(world)))
            {
                new WorldCreator(world).createWorld();
            }
        });
    }

    private @NotNull @Unmodifiable Set<String> getLoadedWorlds()
    {
        return Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toUnmodifiableSet());
    }
}
