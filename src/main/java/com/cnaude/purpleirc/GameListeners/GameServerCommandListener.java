/*
 * Copyright (C) 2014 cnaude
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cnaude.purpleirc.GameListeners;

import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;
import com.google.common.base.Joiner;
import net.minecraft.command.server.CommandBroadcast;
import net.minecraft.command.server.CommandEmote;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 *
 * @author cnaude
 */
public class GameServerCommandListener {

    private final PurpleIRC plugin;

    /**
     *
     * @param plugin
     */
    public GameServerCommandListener(PurpleIRC plugin) {
        this.plugin = plugin;
    }

    @SubscribeEvent
    public void onServerCommand(CommandEvent event) {
        if (event.getCommand() instanceof CommandEmote) {
            if (event.getSender() instanceof EntityPlayer) {
                EntityPlayerMP player = (EntityPlayerMP) event.getSender();
                String msg = Joiner.on(" ").join(event.getParameters());
                for (PurpleBot ircBot : plugin.ircBots.values()) {
                    ircBot.gameAction(player, msg);
                }
            }
        } else if (event.getCommand() instanceof CommandBroadcast) {
            String msg = Joiner.on(" ").join(event.getParameters());
            String cmd = event.getCommand().getCommandName();
            if (cmd.equals("say")) {
                for (PurpleBot ircBot : plugin.ircBots.values()) {
                    ircBot.consoleChat(msg);
                }
            } else if (cmd.equals("broadcast")) {
                for (PurpleBot ircBot : plugin.ircBots.values()) {
                    ircBot.consoleBroadcast(msg);
                }
            }
        }
    }

}
