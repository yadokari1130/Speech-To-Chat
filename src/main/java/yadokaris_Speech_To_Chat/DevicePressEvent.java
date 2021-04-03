package yadokaris_Speech_To_Chat;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DevicePressEvent {

	public static KeyBinding recognizeKey = new KeyBinding("yadokaris_s2c.key.Recognize", Keyboard.KEY_V, "Speech To Chat");

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void KeyHandlingEvent(KeyInputEvent event) {
		if (recognizeKey.isPressed()) {
			ConfigCommand.doRecognize = !ConfigCommand.doRecognize;
			if (ConfigCommand.doRecognize) new Thread(new ConfigCommand()).start();
			S2C.player.sendMessage(new TextComponentString(ConfigCommand.doRecognize ? "Recognize start" : "Recognize stop"));
			Display.setTitle(S2C.title + (ConfigCommand.doRecognize ? " (Recognizing)" : ""));
		}
	}
}
