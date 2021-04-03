package yadokaris_Speech_To_Chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.gmail.game.yadokari1130.JavaSpeechAPI;
import com.google.gson.Gson;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ConfigCommand implements ICommand, Runnable {

	static boolean doRecognize = false;
	static JavaSpeechAPI speech = new JavaSpeechAPI();

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "s2c";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/s2c";
	}

	@Override
	public List<String> getAliases() {
		List<String> list = new ArrayList<>();
		list.add("s2c");
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			if (args[0].equals("alias")) {
				if (args[1].equals("show")) {
					for (String key : ((Map<String, String>)S2C.map.get("aliases")).keySet()) {
						sender.sendMessage(new TextComponentString(key + " -> " + ((Map<String, String>)S2C.map.get("aliases")).get(key)));
					}
					if (((Map<String, String>)S2C.map.get("aliases")).isEmpty()) sender.sendMessage(new TextComponentString("Not registered"));
					return;
				}
				else if (args[1].equals("set")) {
					if (!Arrays.asList(args).contains("->")) {
						sender.sendMessage(new TextComponentString("Please type \"->\" between the texts").setStyle(new Style().setColor(TextFormatting.RED)));
						return;
					}
					StringBuilder sb = new StringBuilder();
					int i = 2;
					for (; !args[i].equals("->"); i++) {
						sb.append(args[i] + " ");
					}
					String text1 = sb.toString().substring(0, sb.length() - 1);
					sb = new StringBuilder();
					for (i++; i < args.length; i++) {
						sb.append(args[i] + " ");
					}
					String text2 = sb.toString().substring(0, sb.length() - 1);
					((Map<String, String>)S2C.map.get("aliases")).put(text1, text2);
				}
				else if (args[1].equals("delete")) {
					StringBuilder sb = new StringBuilder();
					for (int i = 2; i < args.length; i++) {
						sb.append(args[i] + " ");
					}
					String text = sb.toString().substring(0, sb.length() - 1);
					if (((Map<String, String>)S2C.map.get("aliases")).containsKey(text)) ((Map<String, String>)S2C.map.get("aliases")).remove(text);
					else {
						sender.sendMessage(new TextComponentString("The word isn't registered").setStyle(new Style().setColor(TextFormatting.RED)));
						return;
					}
				}
			}
			else if (args[0].equals("before")) {
				if (args[1].equals("show")) {
					sender.sendMessage(new TextComponentString(S2C.map.get("before").toString().equals("") ? "Not registered" : S2C.map.get("before").toString()));
					return;
				}
				else if (args[1].equals("delete")) S2C.map.put("before", "");
				else if (args[1].equals("set")) {
					StringBuilder sb = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						sb.append(args[i] + " ");
					}
					S2C.map.put("before", sb.toString());
				}
			}
			else {
				sender.sendMessage(new TextComponentString("Invalid argument").setStyle(new Style().setColor(TextFormatting.RED)));
				return;
			}
			Gson gson = new Gson();
			String json = gson.toJson(S2C.map);
			try {
				FileUtils.write(new File(S2C.path), json, "UTF-8", false);
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			sender.sendMessage(new TextComponentString("Saved " + args[0]).setStyle(new Style().setColor(TextFormatting.GREEN)));
		}
		catch (IndexOutOfBoundsException e) {
			sender.sendMessage(new TextComponentString("Invalid argument").setStyle(new Style().setColor(TextFormatting.RED)));
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		speech.updateText();
		while (doRecognize) {
			if (S2C.player == null) continue;
			if (speech.updateText()) {
				String text = speech.getText();
				for (String key : ((Map<String, String>)S2C.map.get("aliases")).keySet()) {
					text = text.replaceAll(key, ((Map<String, String>)S2C.map.get("aliases")).get(key));
				}
				((EntityPlayerSP)S2C.player).sendChatMessage(S2C.map.get("before") + text);
			}

			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
