package org.yabogvk.notification;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yabogvk.color.Colorizer;

public final class NotificationService {

    private static final String DECORATION_SEPARATOR = " » ";

    private final JavaPlugin plugin;
    private final LegacyComponentSerializer serializer;
    private Colorizer colorizer;

    public NotificationService(final JavaPlugin plugin, final Colorizer colorizer) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacySection();
        this.colorizer = colorizer;
    }

    public void setColorizer(final Colorizer colorizer) {
        this.colorizer = colorizer;
    }

    public void broadcastChat(final String message) {
        final String colorized = this.colorizer.colorize(message);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(colorized);
        }
    }

    public void broadcastActionBar(final String message) {
        final String colorized = this.colorizer.colorize(message);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(this.serializer.deserialize(colorized));
        }
    }

    public void broadcastSound(final Sound sound, final float volume, final float pitch) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void sendInfo(final CommandSender sender, final String prefix, final String message) {
        sender.sendMessage(this.colorizer.colorize(this.decorate(prefix, message)));
        this.playResultSound(sender, true);
    }

    public void sendError(final CommandSender sender, final String prefix, final String message) {
        sender.sendMessage(this.colorizer.colorize(this.decorate(prefix, message)));
        this.playResultSound(sender, false);
    }

    public void broadcastInfo(final String prefix, final String message, final boolean sendActionBar) {
        final String decorated = this.decorate(prefix, message);
        final String colorized = this.colorizer.colorize(decorated);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(colorized);
            if (sendActionBar) {
                player.sendActionBar(this.serializer.deserialize(colorized));
            }
        }
    }

    public void logInfo(final String prefix, final String message) {
        this.plugin.getLogger().info(ChatColor.stripColor(this.colorizer.colorize(this.decorate(prefix, message))));
    }

    private void playResultSound(final CommandSender sender, final boolean success) {
        if (!(sender instanceof Player player)) {
            return;
        }

        final Sound sound = success ? Sound.ENTITY_PLAYER_LEVELUP : Sound.ENTITY_VILLAGER_NO;
        player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    private String decorate(final String prefix, final String message) {
        return prefix + DECORATION_SEPARATOR + message;
    }

}
