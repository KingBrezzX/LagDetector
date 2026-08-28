package me.itzbrezz.lagdetector.detection;

public final class LagSnapshot {

    private final String lagType;
    private final String world;

    private final int x;
    private final int y;
    private final int z;

    private final String player;
    private final long timestamp;

    private final double tps;
    private final double mspt;

    public LagSnapshot(
            String lagType,
            String world,
            int x,
            int y,
            int z,
            String player,
            long timestamp,
            double tps,
            double mspt
    ) {
        this.lagType = lagType;
        this.world = world;

        this.x = x;
        this.y = y;
        this.z = z;

        this.player = player;
        this.timestamp = timestamp;

        this.tps = tps;
        this.mspt = mspt;
    }

    public String getLagType() {
        return lagType;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getPlayer() {
        return player;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getTps() {
        return tps;
    }

    public double getMspt() {
        return mspt;
    }

    public boolean hasPlayer() {
        return player != null
                && !player.isBlank();
    }

    public boolean hasLocation() {
        return world != null
                && !world.isBlank();
    }

    public String getCoordinates() {
        return x + " " + y + " " + z;
    }

    public long getAgeMillis() {
        return Math.max(
                0L,
                System.currentTimeMillis() - timestamp
        );
    }

    @Override
    public String toString() {
        return "LagSnapshot{" +
                "lagType='" + lagType + '\'' +
                ", world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", player='" + player + '\'' +
                ", timestamp=" + timestamp +
                ", tps=" + tps +
                ", mspt=" + mspt +
                '}';
    }
          }
