package com.tfc.tempdiscordmcchatmod;

import net.minecraft.entity.Entity;
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
		
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			if (JDABot.sender.isAlive()) {
				bot.sendMessage("\u2620 Server has crashed!");
				while (!JDABot.messagesToSend.isEmpty());
				JDABot.sender.stop();
				JDABot.isServerStillOn.set(false);
			}
		}));
	}
	
	//FMLServerAboutToStartEvent, FMLServerStartingEvent, FMLServerStartedEvent, FMLServerStoppingEvent, FMLServerStoppedEvent
	
	private void starting(FMLServerAboutToStartEvent event) {
		JDABot.isServerStillOn.set(true);
		
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
			
			if (event.getSource().getImmediateSource() != null) {
				String name = event.getSource().getTrueSource().getName().getUnformattedComponentText();
				if (!name.equals("")) {
					immediateSource = name;
				} else {
					if (event.getSource().getImmediateSource().getEntityString() != null) {
						String[] imSrc = event.getSource().getImmediateSource().getEntityString().split(":", 2);
						immediateSource = imSrc[imSrc.length-1];
					} else {
						immediateSource = event.getSource().getImmediateSource().getType().getRegistryName().getPath();
					}
				}
			}
			
			String trueSource = "";
			
			if (event.getSource().getTrueSource() != null) {
				String name = event.getSource().getTrueSource().getName().getUnformattedComponentText();
				if (!name.equals("")) {
					trueSource = name;
				} else {
					if (event.getSource().getTrueSource().getEntityString() != null) {
						String[] trSrc = event.getSource().getTrueSource().getEntityString().split(":", 2);
						immediateSource = trSrc[trSrc.length-1];
					} else {
						immediateSource = event.getSource().getTrueSource().getType().getRegistryName().getPath();
					}
				}
			}
			
			String type = event.getSource().damageType;
			
			String playerName = event.getEntityLiving().getName().getUnformattedComponentText();
			
			try {
				if (immediateSource.equals("") && trueSource.equals("")) {
					bot.sendAsEmbedWithTitleInSeed(
							getSeed(event.getEntity()),
							playerName + " was killed by " + type,
							getIcon((PlayerEntity) event.getEntity()),
							true
					);
				} else if (!trueSource.equals("")) {
					if (immediateSource.equals("") || immediateSource.equals(trueSource)) {
						if (type.equals("mob")) {
							bot.sendAsEmbedWithTitleInSeed(
									getSeed(event.getEntity()),
									playerName + " was killed by " + trueSource,
									getIcon((PlayerEntity) event.getEntity()),
									true
							);
						} else {
							bot.sendAsEmbedWithTitleInSeed(
									getSeed(event.getEntity()),
									playerName + " was killed by " + trueSource + " using " + type,
									getIcon((PlayerEntity) event.getEntity()),
									true
							);
						}
					} else {
						if (type.equals("mob")) {
							bot.sendAsEmbedWithTitleInSeed(
									getSeed(event.getEntity()),
									playerName + " was killed by " + trueSource + " using " + immediateSource,
									getIcon((PlayerEntity) event.getEntity()),
									true
							);
						} else {
							bot.sendAsEmbedWithTitleInSeed(
									getSeed(event.getEntity()),
									playerName + " was killed by " + trueSource + " using " + immediateSource + " using " + type,
									getIcon((PlayerEntity) event.getEntity()),
									true
							);
						}
					}
				} else if (!immediateSource.equals("")) {
					if (type.equals("mob")) {
						bot.sendAsEmbedWithTitleInSeed(
								getSeed(event.getEntity()),
								playerName + " was killed by " + immediateSource,
								getIcon((PlayerEntity) event.getEntity()),
								true
						);
					} else {
						bot.sendAsEmbedWithTitleInSeed(
								getSeed(event.getEntity()),
								playerName + " was killed by " + immediateSource + " using " + type,
								getIcon((PlayerEntity) event.getEntity()),
								true
						);
					}
				}
			} catch (Throwable ignored) {
				bot.sendAsEmbedWithTitleInSeed(
						getSeed(event.getEntity()),
						playerName + " died.",
						getIcon((PlayerEntity) event.getEntity()),
						true
				);
			}
		}
	}
	
	private void stopping(FMLServerStoppingEvent event) {
		bot.sendMessage("\u274C Server has stopped!");
		while (!JDABot.messagesToSend.isEmpty());
		JDABot.sender.stop();
		JDABot.isServerStillOn.set(false);
	}
	
	private void chat(ServerChatEvent event) {
		bot.sendMessage("<" + event.getUsername() + "> " + event.getMessage());
	}
	
	private void loggedOn(PlayerEvent.PlayerLoggedInEvent event) {
		String name = event.getEntity().getName().getUnformattedComponentText();
		
		bot.sendAsEmbed(
				getSeed(event.getEntity()),
				name + " has joined the game!",
				getIcon(event.getPlayer()),
				true
		);
	}
	
	private void loggedOff(PlayerEvent.PlayerLoggedOutEvent event) {
		String name = event.getEntity().getName().getUnformattedComponentText();
		
		bot.sendAsEmbed(
				getSeed(event.getEntity()),
				name + " has left the game!",
				getIcon(event.getPlayer()),
				true
		);
	}
	
	public static String getSeed(Entity entity) {
		String str =
				entity.getType().getRegistryName().toString() +
						entity.getName().getUnformattedComponentText() +
						entity.getUniqueID().toString() +
						entity.getType().getClassification().getString() +
						entity.getClass().toString();
		return
				str +
						(str.hashCode() * str.hashCode()) + "" +
						(str.hashCode() * str.length()) + "" +
						(str.length() * str.length()) + "" +
						str.length() + "" +
						str.hashCode()
				;
	}
	
	public static String getIcon(PlayerEntity entity) {
		return "https://crafatar.com/avatars/" + entity.getUniqueID().toString().replace("-", "") + "?size=128&default=MHF_Steve&overlay";
	}
}
