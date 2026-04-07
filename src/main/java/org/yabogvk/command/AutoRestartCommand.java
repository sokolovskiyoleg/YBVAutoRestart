package org.yabogvk.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.YBVAutoRestart;
import org.yabogvk.command.admin.DelayCommand;
import org.yabogvk.command.admin.NowCommand;
import org.yabogvk.command.admin.ReloadCommand;
import org.yabogvk.command.admin.StatusCommand;
import org.yabogvk.command.admin.StopCommand;

public final class AutoRestartCommand implements TabExecutor {

    private final YBVAutoRestart plugin;
    private final Map<String, AdminSubcommand> subcommands = new LinkedHashMap<>();

    public AutoRestartCommand(final YBVAutoRestart plugin) {
        this.plugin = plugin;
        this.register(new StatusCommand(plugin));
        this.register(new ReloadCommand(plugin));
        this.register(new NowCommand(plugin));
        this.register(new DelayCommand(plugin));
        this.register(new StopCommand(plugin));
    }

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String label,
        @NotNull final String[] args
    ) {
        if (args.length == 0) {
            this.subcommands.get("status").execute(sender, args);
            return true;
        }

        final AdminSubcommand subcommand = this.subcommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subcommand == null) {
            this.plugin.getNotificationService().sendError(
                sender,
                this.plugin.getRestartScheduler().getPrefix(),
                this.plugin.getRestartScheduler().formatUserMessage(
                    "command.common.unknown-subcommand",
                    Map.of("{LABEL}", label)
                )
            );
            return true;
        }

        subcommand.execute(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String alias,
        @NotNull final String[] args
    ) {
        if (args.length == 1) {
            final String prefix = args[0].toLowerCase(Locale.ROOT);
            return this.subcommands.values().stream()
                .filter(subcommand -> sender.hasPermission(subcommand.getPermission()))
                .map(AdminSubcommand::getName)
                .filter(name -> name.startsWith(prefix))
                .toList();
        }

        if (args.length > 1) {
            final AdminSubcommand subcommand = this.subcommands.get(args[0].toLowerCase(Locale.ROOT));
            if (subcommand != null) {
                return subcommand.tabComplete(args);
            }
        }

        return new ArrayList<>();
    }

    private void register(final AdminSubcommand subcommand) {
        this.subcommands.put(subcommand.getName(), subcommand);
    }
}
