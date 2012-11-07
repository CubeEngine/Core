package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Bans a player
 *
 * @author Phillip Schichtel
 */
public class BanPunishment implements Punishment
{
    public String getName()
    {
        return "ban";
    }

    public void punish(Player player, ConfigurationSection config)
    {
        player.setBanned(true);
        player.kickPlayer(ChatFormat.parseFormats(config.getString("reason", "&cYou were banned as a punishment!")));
    }
}
