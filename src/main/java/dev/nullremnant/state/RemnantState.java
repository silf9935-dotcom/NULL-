package dev.nullremnant.state;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;
public final class RemnantState extends PersistentState {
 private int day,stage,fear,awareness,intrusion;
 private long lastEvent=-1,lastSleep=-1;
 private boolean nightmareSeen,finaleStarted;
 public static RemnantState load(NbtCompound n){RemnantState s=new RemnantState();s.day=n.getInt("day");s.stage=n.getInt("stage");s.fear=n.getInt("fear");s.awareness=n.getInt("awareness");s.intrusion=n.getInt("intrusion");s.lastEvent=n.getLong("lastEvent");s.lastSleep=n.getLong("lastSleep");s.nightmareSeen=n.getBoolean("nightmareSeen");s.finaleStarted=n.getBoolean("finaleStarted");return s;}
 public NbtCompound writeNbt(NbtCompound n){n.putInt("day",day);n.putInt("stage",stage);n.putInt("fear",fear);n.putInt("awareness",awareness);n.putInt("intrusion",intrusion);n.putLong("lastEvent",lastEvent);n.putLong("lastSleep",lastSleep);n.putBoolean("nightmareSeen",nightmareSeen);n.putBoolean("finaleStarted",finaleStarted);return n;}
 public int day(){return day;} public int stage(){return stage;} public int fear(){return fear;} public int awareness(){return awareness;} public int intrusion(){return intrusion;} public long lastEvent(){return lastEvent;} public long lastSleep(){return lastSleep;} public boolean finaleStarted(){return finaleStarted;}
 public void day(int v){day=v;markDirty();} public void stage(int v){stage=v;markDirty();} public void fear(int v){fear=Math.max(0,fear+v);markDirty();} public void awareness(int v){awareness=Math.max(0,awareness+v);markDirty();} public void intrusion(int v){intrusion=Math.max(0,intrusion+v);markDirty();} public void lastEvent(long v){lastEvent=v;markDirty();} public void lastSleep(long v){lastSleep=v;markDirty();} public void nightmareSeen(){nightmareSeen=true;markDirty();} public void finaleStarted(boolean v){finaleStarted=v;markDirty();}
}