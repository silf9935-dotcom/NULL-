package dev.nullremnant.command;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import dev.nullremnant.state.StateAccess;
import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.entity.RemnantEntities;
import dev.nullremnant.entity.RemnantEntity;
import net.minecraft.server.network.ServerPlayerEntity;
public final class RemnantCommands {
 private RemnantCommands(){}
 public static void register(){
  CommandRegistrationCallback.EVENT.register((dispatcher,registryAccess,environment)->dispatcher.register(CommandManager.literal("remnant")
   .then(CommandManager.literal("lore").executes(ctx->{ServerCommandSource source=ctx.getSource();int stage=StateAccess.get(source.getWorld()).stage();String[] entries={"§8The first world was quiet. Too quiet to be empty.","§7A shape was seen beyond the trees, then never again.","§8The signs began appearing near places you had visited.","§cIt copies the things you build. It cannot understand why.","§4It believes this world belongs to the one who returns."};source.sendFeedback(()->Text.literal(entries[Math.min(stage,4)]),false);return Command.SINGLE_SUCCESS;}))
   .then(CommandManager.literal("reload").requires(s->s.hasPermissionLevel(2)).executes(ctx->{RemnantConfig.load();ctx.getSource().sendFeedback(()->Text.literal("NULL Remnant config reloaded."),true);return Command.SINGLE_SUCCESS;}))
   .then(CommandManager.literal("status").requires(s->s.hasPermissionLevel(2)).executes(ctx->{var state=StateAccess.get(ctx.getSource().getWorld());ctx.getSource().sendFeedback(()->Text.literal("NULL Remnant | day "+state.day()+" | stage "+state.stage()+" | fear "+state.fear()+" | awareness "+state.awareness()),false);return Command.SINGLE_SUCCESS;}))
   .then(CommandManager.literal("spawn").requires(s->s.hasPermissionLevel(2)).executes(ctx->{ServerCommandSource source=ctx.getSource();ServerPlayerEntity player=source.getPlayerOrThrow();RemnantEntity entity=RemnantEntities.REMNANT.create(source.getWorld());if(entity==null)return 0;entity.refreshPositionAndAngles(player.getX()+5,player.getY(),player.getZ()+5,0,0);entity.setTarget(player);source.getWorld().spawnEntity(entity);source.sendFeedback(()->Text.literal("Spawned Remnant for testing."),false);return Command.SINGLE_SUCCESS;}))
  ));
 }
}