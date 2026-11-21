package dev.nullremnant.config;

public final class RemnantConfig {
    public static boolean enabled = true;
    public static boolean messages = true;
    public static boolean subtleAnomalies = true;
    public static int eventFrequency = 100;
    public static int minimumStage = 1;
    private RemnantConfig() {}
    public static int clampFrequency(int value) { return Math.max(0, Math.min(200, value)); }
}
