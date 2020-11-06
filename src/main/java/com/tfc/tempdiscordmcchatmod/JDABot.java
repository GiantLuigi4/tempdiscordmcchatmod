package com.tfc.tempdiscordmcchatmod;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

public class JDABot extends ListenerAdapter {
	public JDABot bot;
	public JDA botBuilt;
	private TextChannel channel;
	private static final Logger LOGGER = LogManager.getLogger();
	private boolean showMOTD;
	private boolean showIP;
	private String channelID;
	
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
		
		PropertiesReader reader = new PropertiesReader(f);
		
		showMOTD = Boolean.parseBoolean(reader.getValue("showMOTD"));
		showMOTD = Boolean.parseBoolean(reader.getValue("showIP"));
		
		boolean enabled = Boolean.parseBoolean(reader.getValue("enabled"));
		if (!enabled) return;
		
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		String token = reader.getValue("bot_token");
		
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
			
			channelID = reader.getValue("channel");
			channel = botBuilt.getTextChannelById(channelID);
			
			LOGGER.log(Level.INFO, "Channel ID to send messages to: " + channelID);
			LOGGER.log(Level.INFO, "Channel Name to send messages to: " + channel.getName());
		} catch (Throwable err) {
			err.printStackTrace();
			Runtime.getRuntime().exit(-1);
		}
	}
	
	public void sendMessage(String text) {
		channel.sendMessage(text).complete();
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
		channel.sendMessage(builder.build()).complete();
	}
	
	public void sendAsEmbed(String colorSeed, String title, String iconURL, boolean inLine, String... messages) {
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
		channel.sendMessage(builder.build()).complete();
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
		channel.sendMessage(builder.build()).complete();
	}
	
	public void sendServerStartMSG(MinecraftServer server) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(0, 255, 0));
		String ip = "hidden (or just NYI)";
		
		String ipRead;
		String port;
		try {
			PropertiesReader serverProperties = new PropertiesReader(new File("server.properties"));
			port = serverProperties.getValue("server-port");
			ipRead = serverProperties.getValue("server-ip");
			if (ipRead == null)
				ipRead = ""+InetAddress.getLocalHost().getHostName();
			if (ipRead == null || ipRead.equals(""))
				ipRead = ""+InetAddress.getLocalHost().getHostAddress();
			System.out.println(ipRead);
		} catch (Throwable ignored) {
		}
		
		builder.addField("\u2705 **The server has started!**", "**IP:** " + ip, false);
		if (showMOTD)
			builder.addField("**MOTD**", server.getMOTD(), true);
		
		channel.sendMessage(builder.build()).complete();
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
}
