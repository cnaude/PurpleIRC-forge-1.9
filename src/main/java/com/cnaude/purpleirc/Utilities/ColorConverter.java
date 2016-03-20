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
package com.cnaude.purpleirc.Utilities;

import com.cnaude.purpleirc.PurpleIRC;
import net.minecraft.util.text.TextFormatting;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.pircbotx.Colors;

/**
 *
 * @author cnaude
 */
public class ColorConverter {

    PurpleIRC plugin;
    private final boolean stripGameColors;
    private final boolean stripIRCColors;
    private final boolean stripIRCBackgroundColors;
    private final HashMap<TextFormatting, String> ircColorMap = new HashMap<>();
    private final HashMap<String, TextFormatting> gameColorMap = new HashMap<>();
    private final Pattern bgColorPattern;
    private final Pattern singleDigitColorPattern;
    private final Pattern stripColorPattern;
    private final Pattern colorHack;
    
    public final char COLOR_CHAR = '\u00A7';
    


    /**
     *
     * @param plugin
     * @param stripGameColors
     * @param stripIRCColors
     * @param stripIRCBackgroundColors
     */
    public ColorConverter(PurpleIRC plugin, boolean stripGameColors, boolean stripIRCColors, boolean stripIRCBackgroundColors) {
        this.stripGameColors = stripGameColors;
        this.stripIRCColors = stripIRCColors;
        this.stripIRCBackgroundColors = stripIRCBackgroundColors;
        this.plugin = plugin;
        buildDefaultColorMaps();
        this.bgColorPattern = Pattern.compile("((\\u0003\\d+),\\d+)");
        this.singleDigitColorPattern = Pattern.compile("((\\u0003)(\\d))\\D+");
        this.colorHack = Pattern.compile("((\\u0003\\d+)(,\\d+))\\D");
        this.stripColorPattern = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");
    }

    /**
     *
     * @param message
     * @return
     */
    public String gameColorsToIrc(String message) {
        if (stripGameColors) {
            return ChatColor.stripColor(message);
        } else {
            String newMessage = message;
            for (TextFormatting gameColor : ircColorMap.keySet()) {
                newMessage = newMessage.replace(gameColor.toString(), ircColorMap.get(gameColor));
            }
            // We return the message with the remaining MC color codes stripped out
            return ChatColor.stripColor(newMessage);
        }
    }

    /**
     *
     * @param message
     * @return
     */
    public String ircColorsToGame(String message) {
        Matcher matcher;
        if (stripIRCBackgroundColors) {
            matcher = bgColorPattern.matcher(message);
            while (matcher.find()) {
                plugin.logDebug("Strip bg color: " + matcher.group(1) + " => " + matcher.group(2));
                message = message.replace(matcher.group(1), matcher.group(2));
            }
        }
        matcher = singleDigitColorPattern.matcher(message);
        while (matcher.find()) {
            plugin.logDebug("Single to double: " + matcher.group(3) + " => "
                    + matcher.group(2) + "0" + matcher.group(3));
            // replace \u0003N with \u00030N
            message = message.replace(matcher.group(1), matcher.group(2) + "0" + matcher.group(3));
        }
        matcher = colorHack.matcher(message);
        while (matcher.find()) {
            plugin.logDebug("Silly IRC colors: " + matcher.group(1) + " => "
                    + matcher.group(2));
            // replace \u0003N,N with \u00030N
            message = message.replace(matcher.group(1), matcher.group(2));
        }

        if (stripIRCColors) {
            return Colors.removeFormattingAndColors(message);
        } else {
            String newMessage = message;
            for (String ircColor : gameColorMap.keySet()) {
                newMessage = newMessage.replace(ircColor, gameColorMap.get(ircColor).toString());
            }
            // We return the message with the remaining IRC color codes stripped out
            return Colors.removeFormattingAndColors(newMessage);
        }
    }

    public void addIrcColorMap(String gameColor, String ircColor) {
        TextFormatting chatColor;
        try {
            chatColor = ChatColor.valueOf(gameColor.toUpperCase());
            if (ircColor.equalsIgnoreCase("strip") && ircColorMap.containsKey(chatColor)) {
                plugin.logDebug("addIrcColorMap: " + ircColor + " => " + gameColor);
                ircColorMap.remove(chatColor);
                return;
            }
        } catch (Exception ex) {
            plugin.logError("Invalid game color: " + gameColor);
            return;
        }
        if (chatColor != null) {
            plugin.logDebug("addIrcColorMap: " + gameColor + " => " + ircColor);
            ircColorMap.put(chatColor, getIrcColor(ircColor));
        }
    }

    public void addGameColorMap(String ircColor, String gameColor) {
        if (gameColor.equalsIgnoreCase("strip") && gameColorMap.containsKey(getIrcColor(ircColor))) {
            plugin.logDebug("addGameColorMap: " + ircColor + " => " + gameColor);
            gameColorMap.remove(getIrcColor(ircColor));
            return;
        }
        TextFormatting chatColor;
        try {
            chatColor = ChatColor.valueOf(gameColor.toUpperCase());
        } catch (Exception ex) {
            plugin.logError("Invalid game color: " + gameColor);
            return;
        }
        plugin.logDebug("addGameColorMap: " + ircColor + " => " + gameColor);
        gameColorMap.put(getIrcColor(ircColor), chatColor);
    }

    private String getIrcColor(String ircColor) {
        String s = "";
        try {
            s = (String) Colors.class.getField(ircColor.toUpperCase()).get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            plugin.logError(ex.getMessage());
        }
        if (s.isEmpty()) {
            plugin.logError("Invalid IRC color: " + ircColor);
        }
        return s;
    }

    private void buildDefaultColorMaps() {
        ircColorMap.put(TextFormatting.AQUA, Colors.CYAN);
        ircColorMap.put(TextFormatting.BLACK, Colors.BLACK);
        ircColorMap.put(TextFormatting.BLUE, Colors.BLUE);
        ircColorMap.put(TextFormatting.BOLD, Colors.BOLD);
        ircColorMap.put(TextFormatting.DARK_AQUA, Colors.TEAL);
        ircColorMap.put(TextFormatting.DARK_BLUE, Colors.DARK_BLUE);
        ircColorMap.put(TextFormatting.DARK_GRAY, Colors.DARK_GRAY);
        ircColorMap.put(TextFormatting.DARK_GREEN, Colors.DARK_GREEN);
        ircColorMap.put(TextFormatting.DARK_PURPLE, Colors.PURPLE);
        ircColorMap.put(TextFormatting.DARK_RED, Colors.RED);
        ircColorMap.put(TextFormatting.GOLD, Colors.OLIVE);
        ircColorMap.put(TextFormatting.GRAY, Colors.LIGHT_GRAY);
        ircColorMap.put(TextFormatting.GREEN, Colors.GREEN);
        ircColorMap.put(TextFormatting.LIGHT_PURPLE, Colors.MAGENTA);
        ircColorMap.put(TextFormatting.RED, Colors.RED);
        ircColorMap.put(TextFormatting.UNDERLINE, Colors.UNDERLINE);
        ircColorMap.put(TextFormatting.YELLOW, Colors.YELLOW);
        ircColorMap.put(TextFormatting.WHITE, Colors.WHITE);
        ircColorMap.put(TextFormatting.RESET, Colors.NORMAL);

        gameColorMap.put(Colors.BLACK, TextFormatting.BLACK);
        gameColorMap.put(Colors.BLUE, TextFormatting.BLUE);
        gameColorMap.put(Colors.BOLD, TextFormatting.BOLD);
        gameColorMap.put(Colors.BROWN, TextFormatting.GRAY);
        gameColorMap.put(Colors.CYAN, TextFormatting.AQUA);
        gameColorMap.put(Colors.DARK_BLUE, TextFormatting.DARK_BLUE);
        gameColorMap.put(Colors.DARK_GRAY, TextFormatting.DARK_GRAY);
        gameColorMap.put(Colors.DARK_GREEN, TextFormatting.DARK_GREEN);
        gameColorMap.put(Colors.GREEN, TextFormatting.GREEN);
        gameColorMap.put(Colors.LIGHT_GRAY, TextFormatting.GRAY);
        gameColorMap.put(Colors.MAGENTA, TextFormatting.LIGHT_PURPLE);
        gameColorMap.put(Colors.NORMAL, TextFormatting.RESET);
        gameColorMap.put(Colors.OLIVE, TextFormatting.GOLD);
        gameColorMap.put(Colors.PURPLE, TextFormatting.DARK_PURPLE);
        gameColorMap.put(Colors.RED, TextFormatting.RED);
        gameColorMap.put(Colors.TEAL, TextFormatting.DARK_AQUA);
        gameColorMap.put(Colors.UNDERLINE, TextFormatting.UNDERLINE);
        gameColorMap.put(Colors.WHITE, TextFormatting.WHITE);
        gameColorMap.put(Colors.YELLOW, TextFormatting.YELLOW);
    }
    
    public String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = '\u00A7';
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }
    
}
