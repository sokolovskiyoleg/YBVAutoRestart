package org.yabogvk.notification;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class NotificationService {

    private final JavaPlugin plugin;

    public NotificationService(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void broadcastChat(final String message) {
        final String resolved = this.colorize(message);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(resolved);
        }
    }

    public void broadcastActionBar(final String message) {
        final String resolved = this.colorize(message);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(resolved));
        }
    }

    public void broadcastSound(final Sound sound, final float volume, final float pitch) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void sendInfo(final CommandSender sender, final String prefix, final String message) {
        sender.sendMessage(this.colorize(this.decorate(prefix, message)));
        this.playResultSound(sender, true);
    }

    public void sendError(final CommandSender sender, final String prefix, final String message) {
        sender.sendMessage(this.colorize(this.decorate(prefix, message)));
        this.playResultSound(sender, false);
    }

    public void broadcastInfo(final String prefix, final String message, final boolean sendActionBar) {
        final String resolved = this.colorize(this.decorate(prefix, message));

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(resolved);
            if (sendActionBar) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(resolved));
            }
        }
    }

    public void logInfo(final String prefix, final String message) {
        this.plugin.getLogger().info(ChatColor.stripColor(this.colorize(this.decorate(prefix, message))));
    }

    private void playResultSound(final CommandSender sender, final boolean success) {
        if (!(sender instanceof Player player)) {
            return;
        }

        final Sound sound = success ? Sound.ENTITY_PLAYER_LEVELUP : Sound.ENTITY_VILLAGER_NO;
        player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    private String decorate(final String prefix, final String message) {
        return prefix + " &7» " + message;
    }

    private String colorize(final String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }
}
