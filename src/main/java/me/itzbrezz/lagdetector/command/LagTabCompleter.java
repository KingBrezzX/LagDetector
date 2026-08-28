package me.itzbrezz.lagdetector.command;

import me.itzbrezz.lagdetector.LagDetector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class LagTabCompleter implements TabCompleter {

    private final LagDetector plugin;

    public LagTabCompleter(LagDetector plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        if (!sender.hasPermission(
                "lagdetector.use"
        )
                && !sender.hasPermission(
                "lagdetector.admin"
        )) {

            return Collections.emptyList();
        }

        if (args.length == 1) {

            return filter(
                    Arrays.asList(
                            "gui",
                            "history",
                            "reload",
                            "info",
                            "help",
                            "toggle",
                            "scan"
                    ),
                    args[0]
            );
        }

        if (args.length == 2) {

            if (args[0].equalsIgnoreCase(
                    "toggle"
            )) {

                return filter(
                        Arrays.asList(
                                "enable",
                                "disable"
                        ),
                        args[1]
                );
            }

            if (args[0].equalsIgnoreCase(
                    "scan"
            )) {

                return filter(
                        List.of("stop"),
                        args[1]
                );
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(
            List<String> values,
            String input
    ) {

        if (input == null
                || input.isBlank()) {

            return new ArrayList<>(
                    values
            );
        }

        String lower =
                input.toLowerCase();

        List<String> result =
                new ArrayList<>();

        for (String value : values) {

            if (value.toLowerCase()
                    .startsWith(lower)) {

                result.add(value);
            }
        }

        return result;
    }
}
