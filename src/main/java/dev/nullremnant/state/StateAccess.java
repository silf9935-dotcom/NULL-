package dev.nullremnant.state;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
public final class StateAccess {
 private StateAccess(){}
 private static final PersistentState.Type<RemnantState> TYPE=new PersistentState.Type<>(RemnantState::new,RemnantState::load,null);
 public static RemnantState get(ServerWorld w){return w.getPersistentStateManager().getOrCreate(TYPE,"null_remnant");}
}