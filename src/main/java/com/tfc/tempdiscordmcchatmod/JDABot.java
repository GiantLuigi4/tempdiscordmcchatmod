package com.tfc.tempdiscordmcchatmod;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class JDABot extends ListenerAdapter {
	public JDABot bot;
	public JDA botBuilt;
	private TextChannel channel;
	private static final Logger LOGGER = LogManager.getLogger();
	private boolean showMOTD;
	private boolean showIP;
	private String channelID;
	private static final PropertiesReader localizationMessages;
	private static PropertiesReader serverProperties;
	
	public static AtomicBoolean isServerStillOn = new AtomicBoolean(true);
	
	public static final ArrayList<MessageAction> messagesToSend = new ArrayList<>();
	
	public static final Thread sender = new Thread(()->{
		try {
			while (isServerStillOn.get() || !messagesToSend.isEmpty()) {
				try {
					if (!messagesToSend.isEmpty()) {
						messagesToSend.get(0).complete();
						messagesToSend.remove(0);
					}
					Thread.sleep(10);
				} catch (Throwable ignored) {
				}
			}
		} catch (Throwable ignored) {
		}
	});
	
	static {
		try {
			File f = new File("localization.properties");
			if (!f.exists()) {
				f.createNewFile();
				FileWriter writer = new FileWriter(f);
				writer.write("" +
						"was killed by fall:fell from a high place\n" +
						"was killed by creeper using explosion.player:was blown up by creeper\n" +
						"was killed by explosion:blew up\n" +
						"was killed by cactus:was pricked to death\n" +
						"was killed by outOfWorld:fell out of the world\n" +
						"using player:using melee\n" +
						"was killed by drown:drowned" +
						"");
				writer.close();
			}
			localizationMessages = new PropertiesReader(f);
			sender.setDaemon(true);
			sender.start();
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	public JDABot() throws IOException {
		File f = new File("chat_bot.properties");
		
		if (!f.exists()) {
			f.createNewFile();
			
			FileWriter writer = new FileWriter(f);
			writer.write("" +
					"enabled:false\n" +
					"bot_token:xxx.xxx.xxx-xxx\n" +
					"channel:00000000000\n" +
					"showMOTD:true\n" +
					"showIP:false\n"
			);
			
			writer.close();
		}
		
		serverProperties = new PropertiesReader(f);
		
		showMOTD = Boolean.parseBoolean(serverProperties.getValue("showMOTD"));
		showIP = Boolean.parseBoolean(serverProperties.getValue("showIP"));
		
		boolean enabled = Boolean.parseBoolean(serverProperties.getValue("enabled"));
		if (!enabled) return;
		
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		String token = serverProperties.getValue("bot_token");
		
		builder.setToken(token);
		builder.setStatus(OnlineStatus.ONLINE);
		
		try {
			builder.setActivity(Activity.playing("Linking mc to discord while erd's mod causes memory leaks."));
			System.out.println("Set activity successfully.");
		} catch (Throwable ignored) {
			System.out.println("Failed to set activity.");
		}
		
		bot = this;
		builder.addEventListeners(bot);
		
		try {
			botBuilt = builder.build();
			
			LOGGER.log(Level.INFO, "Awaiting connection.");
			botBuilt.awaitStatus(JDA.Status.CONNECTED);
			LOGGER.log(Level.INFO, "Connected.");
			
			channelID = serverProperties.getValue("channel");
			channel = botBuilt.getTextChannelById(channelID);
			
			LOGGER.log(Level.INFO, "Channel ID to send messages to: " + channelID);
			LOGGER.log(Level.INFO, "Channel Name to send messages to: " + channel.getName());
		} catch (Throwable err) {
			err.printStackTrace();
			Runtime.getRuntime().exit(-1);
		}
	}
	
	public void sendMessage(String text) {
		messagesToSend.add(channel.sendMessage(text));
	}
	
	public void sendAsEmbed(String colorSeed, String title, boolean inLine, String... messages) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(
				new Color(
						((int) Math.abs(colorSeed.length() * 3732.12382f)) % 255,
						Math.abs(Objects.hash(colorSeed)) % 255,
						Math.abs(Objects.hash(colorSeed.toLowerCase())) % 255
				)
		);
		builder.addField(title, "", false);
		for (String message : messages)
			builder.addField("", message, inLine);
		messagesToSend.add(channel.sendMessage(builder.build()));
	}
	
	public void sendAsEmbed(String colorSeed, String title, String iconURL, boolean inLine, String... messages) {
		try {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setColor(
					new Color(
							((int) Math.abs(colorSeed.length() * 3732.12382f)) % 255,
							Math.abs(Objects.hash(colorSeed)) % 255,
							Math.abs(Objects.hash(colorSeed.toLowerCase())) % 255
					)
			);
			builder.setAuthor(title, null, iconURL);
			for (String message : messages)
				builder.addField("", message, inLine);
			messagesToSend.add(channel.sendMessage(builder.build()));
		} catch (Throwable err) {
			System.out.println(iconURL);
		}
	}
	
	public void sendAsEmbedWithTitleInSeed(String colorSeed, String title, String iconURL, boolean inLine, String... messages) {
		EmbedBuilder builder = new EmbedBuilder();
		colorSeed = colorSeed + title;
		builder.setColor(
				new Color(
						((int) Math.abs(colorSeed.length() * 3732.12382f)) % 255,
						Math.abs(Objects.hash(colorSeed)) % 255,
						Math.abs(Objects.hash(colorSeed.toLowerCase())) % 255
				)
		);
		builder.setAuthor(title, null, iconURL);
		for (String message : messages)
			builder.addField("", message, inLine);
		messagesToSend.add(channel.sendMessage(builder.build()));
	}
	
	public void sendServerStartMSG(MinecraftServer server) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(0, 255, 0));
		String ip = "NYI";
		
		String val = "";
		
		if (showIP) {
			String ipRead="";
			String port="";
			try {
				PropertiesReader serverProperties = new PropertiesReader(new File("server.properties"));
				port = serverProperties.getValue("server-port");
				ipRead = serverProperties.getValue("server-ip");
				if (ipRead == null || ipRead.equals("")) ipRead = ""+InetAddress.getLocalHost().getHostAddress();
				if (ipRead.equals("")) ipRead = ""+InetAddress.getLocalHost().getHostAddress();
			} catch (Throwable ignored) {
			}
			System.out.println(ipRead);
//			ip = ipRead;
//			if (!port.equals("")) ip+=":"+port;
			val = "**IP:** " + ip;
		}
		
		builder.addField("\u2705 **The server has started!**", val, false);
		try {
			if (showMOTD)
				builder.addField("**MOTD**", server.getMOTD(), true);
		} catch (Throwable ignored) {
			builder.addField("", "Server is probably not a server, but instead a single player world.", true);
		}
		
		messagesToSend.add(channel.sendMessage(builder.build()));
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) {
			if (event.getChannel().getId().equals(channelID)) {
				IFormattableTextComponent textComponent = new StringTextComponent(
						"<" +
								event.getMessage().getAuthor().getName() +
								TextFormatting.BLUE + ":Discord" +
								TextFormatting.RESET + "> "
				).appendString(event.getMessage().getContentDisplay());
				List<ServerPlayerEntity> playerEntityList = Tempdiscordmcchatmod.server.getPlayerList().getPlayers();
				for (ServerPlayerEntity player : playerEntityList) {
					player.sendStatusMessage(textComponent, false);
				}
			}
		}
	}
	
	public String getAndApplySkinService(Entity entity) {
		String skinService = serverProperties.getValue("skinService");
		skinService = skinService
				.replace("%randomUUID%",new UUID(System.currentTimeMillis(),System.nanoTime()).toString())
				.replace("%uuid_noDash%",entity.getUniqueID().toString().replace("-",""))
				.replace("%uuid%",entity.getUniqueID().toString())
				.replace("%uname%",entity.getName().getUnformattedComponentText())
				;
		return skinService;
	}
	
	public void shutdown() {
		botBuilt.shutdown();
	}
	
	public String sendImage(BufferedImage img) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(img,"png",output);
		Message msg = botBuilt.getTextChannelById(serverProperties.getValue("skinDumpChannel")).sendFile(output.toByteArray(),"skin.png").complete();
//		msg.delete().complete();
		return msg.getAttachments().get(0).getUrl();
	}
}
