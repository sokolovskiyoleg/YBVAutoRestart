package org.yabogvk.command;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface AdminSubcommand {

    String getName();

    String getPermission();

    void execute(CommandSender sender, String[] args);

    default List<String> tabComplete(final String[] args) {
        return List.of();
    }
}
