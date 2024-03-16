/**
 * Copyright © 2024 EjurisYT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jurisss.mbautorejoin;

import net.minecraft.client.Minecraft;
import com.jurisss.mbautorejoin.ChatColor;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.EnumDyeColor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.rmi.CORBA.Util;

public class CommandAutoRejoin extends CommandBase {
    private static boolean autoRejoinEnabled = false;
    private static boolean inLobby = false;
    private static boolean disableAutoRejoin = false;
    private static final int CHECK_DELAY = 3000;
    private String currentTeam;
    private static int yLevel = 0;
    private String lastMessage = "";
    public static boolean teamDetectionMessageSent = false;
    public static boolean lobbyDetectionMessageSent = false;
    public static boolean gameDetectionMessageSent = false;
    public static boolean bedDetectionMessageSent = false;

    private static final List<String> warplobby = Arrays.asList(
            "Connecting to bw-lobby-",
            "Connecting to sw-lobby-",
            "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
   );

    private List<String> notInLobby;

    private void initializeNotInLobby() {
        String playerName = Minecraft.getMinecraft().thePlayer.getName();
        this.notInLobby = Arrays.asList(
                "Starting in \\d+ seconds...",
                playerName + " has joined"
        );
        this.currentTeam = null;
    }

    private boolean niLobby(String message) {
        if (this.notInLobby == null) {
            initializeNotInLobby();
        }
        return this.notInLobby.stream().anyMatch(message::contains);
    }
    private boolean warpingtolobby(String message) {
        return this.warplobby.stream().anyMatch(message::contains);
    }

    public static boolean isInLobby() {
        return inLobby;
    }

    public static void setInLobby(boolean value) {
        inLobby = value;
    }

    public static boolean isAutoRejoinEnabled() {
        return autoRejoinEnabled;
    }

    public static void setAutoRejoinEnabled(boolean value) {
        autoRejoinEnabled = value;
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @Override
    public String getCommandName() {
        return "autorejoin";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/autorejoin <on/off>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.addChatMessage(new ChatComponentText("Usage: /autorejoin <on/off>"));
            return;
        }

        String mode = args[0].toLowerCase();

        if (mode.equals("on")) {
            setAutoRejoinEnabled(true);
            sender.addChatMessage(new ChatComponentText("AutoRejoin is now enabled."));
        } else if (mode.equals("off")) {
            setAutoRejoinEnabled(false);
            sender.addChatMessage(new ChatComponentText("AutoRejoin is now disabled."));
        } else {
            sender.addChatMessage(new ChatComponentText("Invalid argument. Use 'on' or 'off'."));
        }
    }

    public static void checkAutoRejoin(EntityPlayerSP player, BlockPos pos, World world) {
        if (!disableAutoRejoin && isAutoRejoinEnabled() && !isInLobby() && player.posY < yLevel) {
            player.addChatMessage(new ChatComponentText("Autorejoin triggered"));
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/leave");

            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(950);
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/rejoin");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            setAutoRejoinEnabled(false);

            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(13000);
                    setAutoRejoinEnabled(true);
                    player.addChatMessage(new ChatComponentText("[Debug] enabled"));
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    public CommandAutoRejoin() {
        MinecraftForge.EVENT_BUS.register(this);
    }    

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        String message = ChatColor.stripColor(event.message.getUnformattedText());
    
        if (message.equals(lastMessage)) {
            return;
        }
    
        lastMessage = message;
    
        if (!teamDetectionMessageSent && !isInLobby() && message.contains("Starting in 1 seconds...")) {
            this.currentTeam = null;
            if (disableAutoRejoin) {
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(7500);
                        disableAutoRejoin = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
    
            teamDetectionMessageSent = true;
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Detecting your team."));
    
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(5000);
                    yLevel = (int) Minecraft.getMinecraft().thePlayer.posY - 15;
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[Debug] " + yLevel));
                    checkHelmetColorAfterDelay(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld);
                    teamDetectionMessageSent = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else if (!lobbyDetectionMessageSent && warpingtolobby(message)) {
            disableAutoRejoin = true;
            setInLobby(true);
            lobbyDetectionMessageSent = true;
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("You are in the lobby, temporarily disabling AutoRejoin"));
        
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(50);
                    lobbyDetectionMessageSent = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else if (!gameDetectionMessageSent && niLobby(message)) {
            setInLobby(false);
            disableAutoRejoin = false;
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("No longer in the lobby"));
            gameDetectionMessageSent = true;
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(50);
                    gameDetectionMessageSent = false;
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Autorejoin aaaa"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else if (!bedDetectionMessageSent && this.currentTeam != null && message.contains(this.currentTeam + " Bed was destroyed")) {
            disableAutoRejoin = true;
            bedDetectionMessageSent = true;
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Your bed was broken, temporarily disabling AutoRejoin."));
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000);
                    bedDetectionMessageSent = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    private void checkHelmetColorAfterDelay(EntityPlayerSP player, World world) { 
        ItemStack helmetStack = player.getCurrentArmor(3);
        if (helmetStack != null && helmetStack.getItem() instanceof ItemArmor) {
            ItemArmor armorItem = (ItemArmor) helmetStack.getItem();
            int colorValue = armorItem.getColor(helmetStack);
    
            System.out.println("Detected Color Value: " + colorValue);
    
            if (colorValue == 16711935 || colorValue == 65535 || colorValue == 65535 || colorValue == 32768
                    || colorValue == 16777215 || colorValue == 255 || colorValue == 16776960 || colorValue == 8421504) {
                displayTeamMessage(getTeamColorName(colorValue));
            }
        }
    }
    
    
    private String getTeamColorName(int colorValue) {
        switch (colorValue) {
            case 16711935:
                return "Pink";
            case 65535:
                return "Red";
            case 32768:
                return "Green";
            case 16777215:
                return "White";
            case 255:
                return "Blue";
            case 16776960:
                return "Yellow";
            case 8421504:
                return "Gray";
            default:
                return "Unknown";
        }
    }
    
    private String getColorCode(String teamColor) {
        switch (teamColor.toLowerCase()) {
            case "black": return "\u00A70";
            case "dark_blue": return "\u00A71";
            case "dark_green": return "\u00A72";
            case "dark_aqua": return "\u00A73";
            case "dark_red": return "\u00A74";
            case "dark_purple": return "\u00A75";
            case "gold": return "\u00A76";
            case "gray": return "\u00A77";
            case "dark_gray": return "\u00A78";
            case "blue": return "\u00A79";
            case "green": return "\u00A7a";
            case "aqua": return "\u00A7b";
            case "red": return "\u00A7c";
            case "light_purple": return "\u00A7d";
            case "yellow": return "\u00A7e";
            case "white": return "\u00A7f";
            default: return "\u00A7f";
        }
    }    

private void displayTeamMessage(String teamColor) {
    this.currentTeam = teamColor;
    String coloredMessage = "You are in " + getColorCode(teamColor) + teamColor + " team.";
    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(coloredMessage));
}
    
    
}
