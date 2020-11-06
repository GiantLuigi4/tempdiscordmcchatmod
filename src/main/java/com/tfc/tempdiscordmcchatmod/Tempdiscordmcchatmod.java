package com.tfc.tempdiscordmcchatmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("tempdiscordmcchatmod")
public class Tempdiscordmcchatmod {
	
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static JDABot bot;
	
	public static MinecraftServer server;
	
	public Tempdiscordmcchatmod() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.addListener(this::starting);
		MinecraftForge.EVENT_BUS.addListener(this::advancementGained);
		MinecraftForge.EVENT_BUS.addListener(this::stopping);
		MinecraftForge.EVENT_BUS.addListener(this::chat);
		MinecraftForge.EVENT_BUS.addListener(this::onPlayerDeath);
		MinecraftForge.EVENT_BUS.addListener(this::loggedOn);
		MinecraftForge.EVENT_BUS.addListener(this::loggedOff);
	}
	
	//FMLServerAboutToStartEvent, FMLServerStartingEvent, FMLServerStartedEvent, FMLServerStoppingEvent, FMLServerStoppedEvent
	
	private void starting(FMLServerAboutToStartEvent event) {
		try {
			bot = new JDABot();
		} catch (Throwable err) {
			err.printStackTrace();
			Runtime.getRuntime().exit(-1);
		}
		
		server = event.getServer();
		
		bot.sendServerStartMSG(server);
	}
	
	private void advancementGained(AdvancementEvent event) {
		if (!event.getAdvancement().getId().toString().contains("recipes")) {
			bot.sendMessage(
					"**" + event.getEntity().getName().getUnformattedComponentText() +
							"** just got the advancement **" +
							event.getAdvancement().getId().toString() + "**"
			);
		}
	}
	
	private void onPlayerDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof PlayerEntity) {
//			if (event.getSource().getTrueSource() != null) {
//				if (event.getSource().getDamageType().equals("mob")) {
//					bot.sendMessage(
//							event.getEntity().getName().getUnformattedComponentText() +
//									" was slain by " +
//									event.getSource().getTrueSource().getType().getTranslationKey()
//					);
//				} else {
//					bot.sendMessage(
//							event.getEntity().getName().getUnformattedComponentText() +
//									" was killed by " +
//									event.getSource().getTrueSource().getType().getTranslationKey() +
//									" using " + event.getSource().getDamageType()
//					);
//				}
//			} else {
//				if (event.getSource().getImmediateSource() != null) {
//					bot.sendMessage(
//							event.getEntity().getName().getUnformattedComponentText() +
//									" was slain by " +
//									event.getSource().getImmediateSource().getType().getTranslationKey()
//					);
//				} else {
//					bot.sendMessage(
//							event.getEntity().getName().getUnformattedComponentText() +
//									" was killed by " +
//									event.getSource().getTrueSource().getType().getName().getUnformattedComponentText() +
//									" using " + event.getSource().getDamageType()
//					);
//				}
//			}
			String immediateSource = "";

			if (event.getSource().getImmediateSource() != null)
				immediateSource = event.getSource().getImmediateSource().getEntityString();

			String trueSource = "";

			if (event.getSource().getTrueSource() != null)
				trueSource = event.getSource().getTrueSource().getEntityString();

			String type = event.getSource().damageType;
			
			String playerName = event.getEntityLiving().getName().getUnformattedComponentText();
			
			if (immediateSource.equals("") && trueSource.equals("")) {
				bot.sendMessage(playerName + " was killed by " + type);
			} else if (!trueSource.equals("")){
				if (immediateSource.equals("")) {
					if (type.equals("mob")) {
						bot.sendMessage(playerName + " was killed by " + trueSource);
					} else {
						bot.sendMessage(playerName + " was killed by " + trueSource + " using " + type);
					}
				} else {
					if (type.equals("mob")) {
						bot.sendMessage(playerName + " was killed by " + trueSource + " using " + immediateSource);
					} else {
						bot.sendMessage(playerName + " was killed by " + trueSource + " using " + immediateSource + " using " + type);
					}
				}
			} else if (!immediateSource.equals("")){
				if (type.equals("mob")) {
					bot.sendMessage(playerName + " was killed by " + immediateSource);
				} else {
					bot.sendMessage(playerName + " was killed by " + immediateSource + " using " + type);
				}
			}
		}
	}
	
	private void stopping(FMLServerStoppingEvent event) {
		bot.sendMessage("\u274C Server has stopped!");
	}
	
	private void chat(ServerChatEvent event) {
		bot.sendMessage("<" + event.getUsername() + "> " + event.getMessage());
	}
	
	private void loggedOn(PlayerEvent.PlayerLoggedInEvent event) {
		String name = event.getEntity().getName().getUnformattedComponentText();
		
		bot.sendAsEmbed(
				event.getEntity().getUniqueID().toString(),
				name + " has joined the game!",
				"https://crafatar.com/avatars/"+event.getEntity().getUniqueID().toString().replace("-","")+"?size=128&default=MHF_Steve&overlay",
				true
		);
	}
	
	private void loggedOff(PlayerEvent.PlayerLoggedOutEvent event) {
		String name = event.getEntity().getName().getUnformattedComponentText();
		
		bot.sendAsEmbed(
				event.getEntity().getUniqueID().toString(),
				name + " has left the game!",
				"https://crafatar.com/avatars/"+event.getEntity().getUniqueID().toString().replace("-","")+"?size=128&default=MHF_Steve&overlay",
				true
		);
	}
}
