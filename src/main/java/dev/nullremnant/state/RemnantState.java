package dev.nullremnant.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public final class RemnantState extends PersistentState {
    private int day, stage, fear, awareness, intrusion;
    private long lastStalker = -1, lastAnomaly = -1, lastHorror = -1;
    private long lastEvent = -1, lastSleep = -1;
    private boolean nightmareSeen, finaleStarted, memoryAnchorSet, memoryEchoPlaced, doorEchoPlaced;
    private int memoryX, memoryY, memoryZ, doorInteractions, doorRepeatCount, doorX, doorY, doorZ;
    private int lastAreaX, lastAreaZ, rememberedAreaX, rememberedAreaZ, areaRevisits;
    private boolean areaInitialized, rememberedAreaSet;
    private long lastAreaEcho = -1;

    public static RemnantState load(NbtCompound n) {
        RemnantState s = new RemnantState();
        s.day = n.getInt("day");
        s.stage = n.getInt("stage");
        s.fear = n.getInt("fear");
        s.awareness = n.getInt("awareness");
        s.intrusion = n.getInt("intrusion");
        s.lastEvent = n.getLong("lastEvent");
        s.lastSleep = n.getLong("lastSleep");
        s.nightmareSeen = n.getBoolean("nightmareSeen");
        s.finaleStarted = n.getBoolean("finaleStarted");
        s.memoryAnchorSet = n.getBoolean("memoryAnchorSet");
        s.memoryEchoPlaced = n.getBoolean("memoryEchoPlaced");
        s.memoryX = n.getInt("memoryX");
        s.memoryY = n.getInt("memoryY");
        s.memoryZ = n.getInt("memoryZ");
        s.doorInteractions = n.getInt("doorInteractions");
        s.doorX = n.getInt("doorX");
        s.doorY = n.getInt("doorY");
        s.doorZ = n.getInt("doorZ");
        s.doorEchoPlaced = n.getBoolean("doorEchoPlaced");
        s.lastStalker = n.getLong("lastStalker");
        s.lastAnomaly = n.getLong("lastAnomaly");
        s.lastHorror = n.getLong("lastHorror");
        s.lastAreaX = n.getInt("lastAreaX"); s.lastAreaZ = n.getInt("lastAreaZ");
        s.rememberedAreaX = n.getInt("rememberedAreaX"); s.rememberedAreaZ = n.getInt("rememberedAreaZ");
        s.areaRevisits = n.getInt("areaRevisits"); s.areaInitialized = n.getBoolean("areaInitialized");
        s.rememberedAreaSet = n.getBoolean("rememberedAreaSet"); s.lastAreaEcho = n.getLong("lastAreaEcho");
        return s;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound n) {
        n.putInt("day", day);
        n.putInt("stage", stage);
        n.putInt("fear", fear);
        n.putInt("awareness", awareness);
        n.putInt("intrusion", intrusion);
        n.putLong("lastEvent", lastEvent);
        n.putLong("lastSleep", lastSleep);
        n.putBoolean("nightmareSeen", nightmareSeen);
        n.putBoolean("finaleStarted", finaleStarted);
        n.putBoolean("memoryAnchorSet", memoryAnchorSet);
        n.putBoolean("memoryEchoPlaced", memoryEchoPlaced);
        n.putInt("memoryX", memoryX);
        n.putInt("memoryY", memoryY);
        n.putInt("memoryZ", memoryZ);
        n.putInt("doorInteractions", doorInteractions);
        n.putInt("doorX", doorX);
        n.putInt("doorY", doorY);
        n.putInt("doorZ", doorZ);
        n.putBoolean("doorEchoPlaced", doorEchoPlaced);
        n.putLong("lastStalker", lastStalker);
        n.putLong("lastAnomaly", lastAnomaly);
        n.putLong("lastHorror", lastHorror);
        n.putInt("lastAreaX", lastAreaX); n.putInt("lastAreaZ", lastAreaZ);
        n.putInt("rememberedAreaX", rememberedAreaX); n.putInt("rememberedAreaZ", rememberedAreaZ);
        n.putInt("areaRevisits", areaRevisits); n.putBoolean("areaInitialized", areaInitialized);
        n.putBoolean("rememberedAreaSet", rememberedAreaSet); n.putLong("lastAreaEcho", lastAreaEcho);
        return n;
    }

    public int day() { return day; }
    public int stage() { return stage; }
    public int fear() { return fear; }
    public int awareness() { return awareness; }
    public int intrusion() { return intrusion; }
    public long lastEvent() { return lastEvent; }
    public long lastSleep() { return lastSleep; }
    public long lastStalker() { return lastStalker; }
    public long lastAnomaly() { return lastAnomaly; }
    public long lastHorror() { return lastHorror; }
    public boolean finaleStarted() { return finaleStarted; }
    public boolean hasSeenNightmare() { return nightmareSeen; }
    public boolean memoryAnchorSet() { return memoryAnchorSet; }
    public boolean memoryEchoPlaced() { return memoryEchoPlaced; }
    public int memoryX() { return memoryX; }
    public int memoryY() { return memoryY; }
    public int memoryZ() { return memoryZ; }
    public int doorInteractions() { return doorInteractions; }
    public int doorRepeatCount() { return doorRepeatCount; }
    public int doorX() { return doorX; }
    public int doorY() { return doorY; }
    public int doorZ() { return doorZ; }
    public boolean doorEchoPlaced() { return doorEchoPlaced; }
    public int areaRevisits() { return areaRevisits; }
    public int rememberedAreaX() { return rememberedAreaX; }
    public int rememberedAreaZ() { return rememberedAreaZ; }
    public boolean rememberedAreaSet() { return rememberedAreaSet; }
    public long lastAreaEcho() { return lastAreaEcho; }

    public void day(int v) { day = v; markDirty(); }
    public void stage(int v) { stage = v; markDirty(); }
    public void fear(int v) { fear = Math.max(0, fear + v); markDirty(); }
    public void awareness(int v) { awareness = Math.max(0, awareness + v); markDirty(); }
    public void intrusion(int v) { intrusion = Math.max(0, intrusion + v); markDirty(); }
    public void lastEvent(long v) { lastEvent = v; markDirty(); }
    public void lastSleep(long v) { lastSleep = v; markDirty(); }
    public void lastStalker(long v) { lastStalker = v; markDirty(); }
    public void lastAnomaly(long v) { lastAnomaly = v; markDirty(); }
    public void lastHorror(long v) { lastHorror = v; markDirty(); }
    public void nightmareSeen() { nightmareSeen = true; markDirty(); }
    public void finaleStarted(boolean v) { finaleStarted = v; markDirty(); }
    public void memoryAnchor(int x, int y, int z) {
        memoryX = x; memoryY = y; memoryZ = z;
        memoryAnchorSet = true; memoryEchoPlaced = false; markDirty();
    }
    public void memoryEchoPlaced(boolean v) { memoryEchoPlaced = v; markDirty(); }
    public void recordDoor(int x, int y, int z) {
        doorInteractions++;
        if (doorX == x && doorY == y && doorZ == z) doorRepeatCount++;
        else doorRepeatCount = 1;
        doorX = x; doorY = y; doorZ = z; markDirty();
    }
    public void doorEchoPlaced(boolean v) { doorEchoPlaced = v; markDirty(); }
    public void sampleArea(int x, int z) {
        if (!areaInitialized) {
            lastAreaX = x; lastAreaZ = z; rememberedAreaX = x; rememberedAreaZ = z;
            areaInitialized = true; rememberedAreaSet = true; markDirty(); return;
        }
        if (x == lastAreaX && z == lastAreaZ) return;
        if (x == rememberedAreaX && z == rememberedAreaZ) areaRevisits++;
        else if (areaRevisits == 0) { rememberedAreaX = x; rememberedAreaZ = z; rememberedAreaSet = true; }
        lastAreaX = x; lastAreaZ = z; markDirty();
    }
    public void lastAreaEcho(long v) { lastAreaEcho = v; markDirty(); }
}
