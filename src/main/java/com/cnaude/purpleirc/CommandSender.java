package com.cnaude.purpleirc;

import com.cnaude.purpleirc.Utilities.ChatColor;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import org.dynmap.permissions.PermissionsHandler;

/**
 *
 * @author cnaude
 */
public class CommandSender {

    ICommandSender sender;
    PurpleIRC plugin;

    public CommandSender(ICommandSender sender, PurpleIRC plugin) {
        this.sender = sender;
        this.plugin = plugin;
    }

    public void sendMessage(String message) {
        if (sender instanceof EntityPlayer) {
            sender.addChatMessage(new TextComponentTranslation(message));
        } else {
            sender.addChatMessage(new TextComponentTranslation(ChatColor.stripColor(message)));
        }

    }

    public boolean hasPermission(String permission) {
        if (sender instanceof EntityPlayer) {
            PermissionsHandler ph = PermissionsHandler.getHandler();
            if (ph != null) {
                return ph.hasPermission(sender.getCommandSenderEntity().getName(), permission);
            }
        }
        return true;
    }

    public EntityPlayer getPlayer() {
        return plugin.getPlayer(sender.getName());
    }

    public boolean isPlayer() {
        return sender instanceof EntityPlayerMP;
    }
}
