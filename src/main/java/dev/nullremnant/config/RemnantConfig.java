package dev.nullremnant.config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import dev.nullremnant.RemnantMod;
import java.nio.file.Files;
import java.nio.file.Path;
public final class RemnantConfig {
 public static boolean enabled=true,nightmares=true,subtleAnomalies=true;
 public static int eventFrequency=100,stalkerFrequency=100;
 private RemnantConfig(){}
 public static void load(){
  Path file=FabricLoader.getInstance().getConfigDir().resolve("null_remnant.json");
  try{
   if(!Files.exists(file)){JsonObject defaults=new JsonObject();defaults.addProperty("enabled",true);defaults.addProperty("nightmares",true);defaults.addProperty("subtleAnomalies",true);defaults.addProperty("eventFrequency",100);defaults.addProperty("stalkerFrequency",100);Files.createDirectories(file.getParent());Files.writeString(file,defaults.toString());}
   JsonObject root=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
   enabled=getBoolean(root,"enabled",true);nightmares=getBoolean(root,"nightmares",true);subtleAnomalies=getBoolean(root,"subtleAnomalies",true);
   eventFrequency=clamp(getInt(root,"eventFrequency",100));stalkerFrequency=clamp(getInt(root,"stalkerFrequency",100));
  }catch(Exception e){RemnantMod.LOGGER.warn("Could not load NULL Remnant config; using defaults",e);}
 }
 private static boolean getBoolean(JsonObject o,String key,boolean fallback){return o.has(key)?o.get(key).getAsBoolean():fallback;}
 private static int getInt(JsonObject o,String key,int fallback){return o.has(key)?o.get(key).getAsInt():fallback;}
 private static int clamp(int v){return Math.max(0,Math.min(200,v));}
}