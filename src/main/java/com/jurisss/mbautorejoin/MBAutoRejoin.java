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

import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;


@Mod(modid = "mbautorejoin", version = "1.0")
public class MBAutoRejoin {

    private static int ticksSinceLastCheck = 0;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new CommandAutoRejoin());
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(new CommandAutoRejoin());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof EntityPlayerSP) {
            ticksSinceLastCheck++;
            if (ticksSinceLastCheck >= 1) {
                ticksSinceLastCheck = 0;
                EntityPlayerSP player = (EntityPlayerSP) event.player;
                CommandAutoRejoin.checkAutoRejoin(player, player.getPosition(), player.getEntityWorld());
            }
        }
    }
    
    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (Minecraft.getMinecraft().getCurrentServerData() == null) return;
        if (Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("mineberry.")) {
            CompletableFuture.runAsync(() -> {
                try {
                    while (Minecraft.getMinecraft().thePlayer == null) {
                        Thread.yield();
                    }
    
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        try {
                            Thread.sleep(2000);
    
                            String username = Minecraft.getMinecraft().thePlayer.getName();
    
                            String messageFormat = "\u00A7l\u00A72====\u00A7r\u00A72Auto Rejoin Mod (For Mineberry BW)\u00A7l\u00A72====\n\n" +
                            "\u00A7r\u00A7eHello \u00A7r\u00A7a%s\u00A7r\u00A7e!\n\n" +
                            "\u00A7r\u00A7eCommands:\n" +
                            "\u00A7r\u00A7a\u2192 \u00A7r/autorejoin (on/off) - Enable/disable auto rejoin\n\n" +
                            "\u00A7r\u00A7bThis Mod Was Made By \u00A7r\u00A73Jurisss\n" +
                            "\n\u00A7r\u00A7eDiscord: discord.gg/KN6fScv6cs\n" +
                            "\u00A7r\u00A7eWebsite: juriss.xyz";    
    
                            String messageText = String.format(messageFormat, username);
                            IChatComponent message = new ChatComponentText(messageText);
                            Minecraft.getMinecraft().thePlayer.addChatMessage(message);
    
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).whenComplete((result, exception) -> {
                ClientCommandHandler.instance.registerCommand(new CommandAutoRejoin());
                FMLCommonHandler.instance().bus().register(new CommandAutoRejoin());
            });
        }
    }    
}
