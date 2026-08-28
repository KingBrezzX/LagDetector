package me.itzbrezz.lagdetector.action;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.List;

public final class SignManager {

    private final LagDetector plugin;

    public SignManager(LagDetector plugin) {
        this.plugin = plugin;
    }

    public boolean createProtectionSign(
            Block block,
            LagSnapshot snapshot
    ) {

        if (block == null || snapshot == null) {
            return false;
        }

        if (!plugin.getConfig().getBoolean(
                "actions.break.sign.enabled",
                true
        )) {
            return false;
        }

        /*
         * Only operate on an already-loaded block.
         */
        if (!block.getWorld().isChunkLoaded(
                block.getChunk().getX(),
                block.getChunk().getZ()
        )) {
            return false;
        }

        if (!block.getType().isAir()) {
            return false;
        }

        block.setType(
                Material.OAK_SIGN,
                false
        );

        if (!(block.getState() instanceof Sign sign)) {
            block.setType(
                    Material.AIR,
                    false
            );
            return false;
        }

        List<String> lines =
                plugin.getConfig().getStringList(
                        "actions.break.sign.lines"
                );

        for (int i = 0; i < 4; i++) {

            String line =
                    i < lines.size()
                            ? lines.get(i)
                            : "";

            line = replacePlaceholders(
                    line,
                    snapshot
            );

            sign.setLine(
                    i,
                    stripColors(
                            plugin.color(line)
                    )
            );
        }

        sign.update(
                true,
                false
        );

        return true;
    }

    private String replacePlaceholders(
            String text,
            LagSnapshot snapshot
    ) {

        return text
                .replace(
                        "%world%",
                        snapshot.getWorld()
                )
                .replace(
                        "%x%",
                        String.valueOf(
                                snapshot.getX()
                        )
                )
                .replace(
                        "%y%",
                        String.valueOf(
                                snapshot.getY()
                        )
                )
                .replace(
                        "%z%",
                        String.valueOf(
                                snapshot.getZ()
                        )
                )
                .replace(
                        "%player%",
                        snapshot.hasPlayer()
                                ? snapshot.getPlayer()
                                : "Unknown"
                )
                .replace(
                        "%lag-type%",
                        snapshot.getLagType()
                );
    }

    private String stripColors(
            String text
    ) {

        return org.bukkit.ChatColor
                .stripColor(text);
    }
            }
