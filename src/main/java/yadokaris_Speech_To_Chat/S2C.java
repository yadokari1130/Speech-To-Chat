package yadokaris_Speech_To_Chat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.Display;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = "yadokaris_speech_to_chat", name = "yadokari's Speech To Chat", version = S2C.version, updateJSON = S2C.jsonURL)
public class S2C {

	static String playerName;
	static EntityPlayer player;
	static final String version = "1.0";
	static final String jsonURL = "https://raw.githubusercontent.com/yadokari1130/Speech-To-Chat/master/update.json";
	static String path;
	static Properties prop = new Properties();
	private static boolean isNotificated = false;
	static Map<String, Object> map = new HashMap<>();
	static String title;

	@SuppressWarnings("unchecked")
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		try {
			Class.forName("org.apache.commons.exec.Executor");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		path = event.getSuggestedConfigurationFile().getParent() + "\\S2C.json";

		Gson gson = new Gson();
		try {
			map = gson.fromJson(FileUtils.readFileToString(new File(path), "UTF-8"), Map.class);
		}
		catch (FileNotFoundException e) {
			map.put("before", "");
			Map<String, String> aliases = new HashMap<>();
			map.put("aliases", aliases);
		}
		catch (JsonSyntaxException | IOException e) {
			e.printStackTrace();
		}

		ClientCommandHandler.instance.registerCommand(new ConfigCommand());

		title = Display.getTitle();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ClientRegistry.registerKeyBinding(DevicePressEvent.recognizeKey);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new DevicePressEvent());

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			ConfigCommand.speech.quit();
		}));
	}

	public static void saveProperties() throws IOException {
		OutputStream writer = new FileOutputStream(path);
		prop.store(writer, "comments");
		writer.flush();
	}

	@SuppressWarnings("unchecked")
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onJoinWorld(EntityJoinWorldEvent event) {
		if (Minecraft.getMinecraft().player != null && event.getEntity().equals(Minecraft.getMinecraft().player)) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			this.player = player;
			this.playerName = player.getName();

			new Thread(() -> {
				String update = null;
				try {
					update = IOUtils.toString(new URL(S2C.jsonURL), "UTF-8");
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				Gson gson = new Gson();
				Map map = gson.fromJson(update, Map.class);

				if (map.isEmpty()) return;

				String latest = ((Map<String, String>) map.get("promos")).get("1.12.2-latest");

				if (!isNotificated && !latest.equals(version)) {
					new Thread(() -> {
						try {
							Thread.sleep(5000);
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
						ClickEvent linkClickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, (String)map.get("homepage"));
						Style clickableStyle = new Style().setClickEvent(linkClickEvent).setColor(TextFormatting.AQUA);
						Style color = new Style().setColor(TextFormatting.GREEN);
						player.sendMessage(new TextComponentTranslation("yadokaris_s2c.update.message1").setStyle(color));
						player.sendMessage(new TextComponentTranslation("yadokaris_s2c.update.message2").setStyle(clickableStyle));
						player.sendMessage(new TextComponentTranslation("yadokaris_s2c.update.infomation"));
						player.sendMessage(new TextComponentString("----------------------------------------------------------------------"));
						player.sendMessage(new TextComponentString(((Map<String, String>) map.get("1.12.2")).get(latest)));
						player.sendMessage(new TextComponentString("----------------------------------------------------------------------"));
					}).start();
					isNotificated = true;
				}
			}).start();
		}
	}
}