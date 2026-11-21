package dev.nullremnant.command;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import dev.nullremnant.state.StateAccess;
import dev.nullremnant.entity.RemnantEntities;
import dev.nullremnant.entity.RemnantEntity;
import net.minecraft.server.network.ServerPlayerEntity;
public final class RemnantCommands {
 private RemnantCommands(){}
 public static void register(){
  CommandRegistrationCallback.EVENT.register((dispatcher,registryAccess,environment)->dispatcher.register(CommandManager.literal("remnant").requires(s->s.hasPermissionLevel(2))
   .then(CommandManager.literal("status").executes(ctx->{var state=StateAccess.get(ctx.getSource().getWorld());ctx.getSource().sendFeedback(()->Text.literal("NULL Remnant | day "+state.day()+" | stage "+state.stage()+" | fear "+state.fear()+" | awareness "+state.awareness()),false);return Command.SINGLE_SUCCESS;}))
   .then(CommandManager.literal("spawn").executes(ctx->{ServerCommandSource source=ctx.getSource();ServerPlayerEntity player=source.getPlayerOrThrow();RemnantEntity entity=RemnantEntities.REMNANT.create(source.getWorld());if(entity==null)return 0;entity.refreshPositionAndAngles(player.getX()+5,player.getY(),player.getZ()+5,0,0);entity.setTarget(player);source.getWorld().spawnEntity(entity);source.sendFeedback(()->Text.literal("Spawned Remnant for testing."),false);return Command.SINGLE_SUCCESS;}))
  ));
 }
}