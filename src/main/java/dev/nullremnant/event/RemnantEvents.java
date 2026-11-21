package dev.nullremnant.event;
import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.RemnantMod;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import java.util.Random;
public final class RemnantEvents {
 private static final Random RNG=new Random();private static final long INTERVAL=12000L;
 private RemnantEvents(){}
 public static void tick(ServerWorld world){
  if(!RemnantConfig.enabled||world.getPlayers().isEmpty())return;RemnantState s=StateAccess.get(world);long t=world.getTime();int d=(int)(t/24000L);
  if(d>s.day())s.day(d);int stage=stage(d);if(stage!=s.stage()){s.stage(stage);announce(world,stage);}
  sleepCheck(world,s,t);
  if(t%INTERVAL==0&&s.lastEvent()!=t){s.lastEvent(t);if(RNG.nextDouble()<=chance(stage)*RemnantConfig.eventFrequency/100.0){int roll=RNG.nextInt(100);switch(stage){case 0->early(world,roll);case 1->awakening(world,roll);case 2->aggression(world,roll);case 3->reality(world,roll);default->finale(world,roll);}}}
  HorrorDirector.tick(world);
  StalkerDirector.tick(world);
  MemorySystem.tick(world);
  if(!world.getPlayers().isEmpty())AnomalyEvents.tryTrigger(world,world.getPlayers().get(0));
 }
 private static int stage(int d){return d<3?0:d<10?1:d<15?2:d<21?3:4;}private static double chance(int s){return new double[]{.10,.24,.40,.52,.64}[s];}
 private static void announce(ServerWorld w,int s){String[] m={"","","§7Something in this world remembers.","§8You are no longer alone.","§cThe world has begun to repeat.","§4The save is not yours alone."};if(s>0)RemnantMod.tell(w,m[s]);if(s==4){RemnantState state=StateAccess.get(w);if(!state.finaleStarted()){state.finaleStarted(true);state.intrusion(10);RemnantMod.tell(w,"§4It has learned where you began.");RemnantMod.tell(w,"§8The next door may not lead back.");}}}
 private static void sleepCheck(ServerWorld w,RemnantState s,long t){if(t%100L!=0||s.lastSleep()==t)return;java.util.List<ServerPlayerEntity> sleepers=w.getPlayers().stream().filter(ServerPlayerEntity::isSleeping).toList();if(sleepers.isEmpty())return;s.lastSleep(t);if(!RemnantConfig.nightmares||s.day()<3||RNG.nextDouble()>=.30)return;ServerPlayerEntity p=sleepers.get(RNG.nextInt(sleepers.size()));s.fear(2);w.playSound(null,p.getBlockPos(),SoundEvents.ENTITY_ENDERMAN_STARE,SoundCategory.AMBIENT,0.18f,0.55f);p.wakeUp(false,false);p.sendMessage(Text.literal("§8You cannot remember falling asleep."),false);if(!s.hasSeenNightmare()){s.nightmareSeen();p.sendMessage(Text.literal("§7A door stands somewhere in the dark."),false);p.sendMessage(Text.literal("§8Behind it, someone is copying your movements."),false);}else if(s.stage()>=3){p.sendMessage(Text.literal("§4You wake up facing the wrong direction."),false);p.sendMessage(Text.literal("§8The door is gone. The feeling remains."),false);}else p.sendMessage(Text.literal("§7You dream of a door in a cave."),false);}
 private static void early(ServerWorld w,int r){if(r<35)RemnantMod.tell(w,"§7The wind sounds like footsteps for a moment.");else if(r<70)RemnantMod.tell(w,"§7A chunk seems to take longer than usual to settle.");else RemnantMod.tell(w,"§7You hear a wooden door somewhere far away.");}
 private static void awakening(ServerWorld w,int r){if(r<25)RemnantMod.tell(w,"§8You hear footsteps beyond the trees.");else if(r<50)cross(w);else if(r<75)sign(w,"I remember this place.");else RemnantMod.tell(w,"§7Something has changed, but you cannot find what.");}
 private static void aggression(ServerWorld w,int r){if(r<20)sign(w,"You always close the door.");else if(r<40)RemnantMod.tell(w,"§8There is a figure at the edge of the fog.");else if(r<60)RemnantMod.tell(w,"§7A door opens somewhere nearby.");else if(r<80){ServerPlayerEntity player=w.getPlayers().get(w.getRandom().nextInt(w.getPlayers().size()));player.sendMessage(Text.literal("§8"+player.getName().getString()+" §7: You came back."),false);}else RemnantMod.tell(w,"§7The save takes longer than it should.");}
 private static void reality(ServerWorld w,int r){if(r<20)sign(w,"You have built this before.");else if(r<40)RemnantMod.tell(w,"§8The landscape looks strangely familiar.");else if(r<60)cross(w);else if(r<80)RemnantMod.tell(w,"§7You hear your own footsteps, a few seconds late.");else RemnantMod.tell(w,"§8The world repeats a sound it should not know.");}
 private static void finale(ServerWorld w,int r){if(r<25)sign(w,"I know how to build a home now.");else if(r<50)RemnantMod.tell(w,"§4You hear a door opening inside your house.");else if(r<75)RemnantMod.tell(w,"§8There is someone standing where you first arrived.");else RemnantMod.tell(w,"§4There is only one player.");}
 private static void cross(ServerWorld w){
  ServerPlayerEntity player=w.getPlayers().get(RNG.nextInt(w.getPlayers().size()));
  BlockPos sample=player.getBlockPos().add(RNG.nextInt(17)-8,0,RNG.nextInt(17)-8);
  if(!w.isChunkLoaded(sample))return;
  BlockPos center=w.getTopPosition(Heightmap.Type.MOTION_BLOCKING,sample);
  if(!w.isChunkLoaded(center))return;
  int emitted=0;
  for(int offset=-2;offset<=2;offset++){
   BlockPos point=center.add(offset,0,0);
   if(w.isChunkLoaded(point)&&w.getBlockState(point).isAir()
      &&w.getBlockState(point.up()).isAir()&&w.getBlockState(point.down()).isSolid()){
    double x=point.getX()+0.5,y=point.getY()+0.15,z=point.getZ()+0.5;
    w.spawnParticles(net.minecraft.particle.ParticleTypes.LARGE_SMOKE,x,y,z,5,0.18,0.12,0.18,0.005);
    w.spawnParticles(net.minecraft.particle.ParticleTypes.SMOKE,x,y,z,8,0.22,0.18,0.22,0.008);
    emitted++;
   }
   point=center.add(0,0,offset);
   if(w.isChunkLoaded(point)&&w.getBlockState(point).isAir()
      &&w.getBlockState(point.up()).isAir()&&w.getBlockState(point.down()).isSolid()){
    double x=point.getX()+0.5,y=point.getY()+0.15,z=point.getZ()+0.5;
    w.spawnParticles(net.minecraft.particle.ParticleTypes.LARGE_SMOKE,x,y,z,5,0.18,0.12,0.18,0.005);
    w.spawnParticles(net.minecraft.particle.ParticleTypes.SMOKE,x,y,z,8,0.22,0.18,0.22,0.008);
    emitted++;
   }
  }
  if(emitted==0)return;
  w.playSound(null,center,SoundEvents.AMBIENT_CAVE,SoundCategory.AMBIENT,0.65f,0.65f);
  player.sendMessage(Text.literal("§8Something has been placed nearby."),false);
 }
 private static void sign(ServerWorld w,String text){ServerPlayerEntity p=w.getPlayers().get(RNG.nextInt(w.getPlayers().size()));BlockPos pos=w.getTopPosition(Heightmap.Type.MOTION_BLOCKING,p.getBlockPos().add(RNG.nextInt(9)-4,0,RNG.nextInt(9)-4));if(!w.getBlockState(pos).isAir()||!w.getBlockState(pos.down()).isSolid())return;w.setBlockState(pos,Blocks.OAK_SIGN.getDefaultState());if(w.getBlockEntity(pos) instanceof SignBlockEntity be){be.changeText(signText->signText.withMessage(0,Text.literal(text)).withMessage(1,Text.empty()),true);be.markDirty();w.updateListeners(pos,w.getBlockState(pos),w.getBlockState(pos),3);}}
}