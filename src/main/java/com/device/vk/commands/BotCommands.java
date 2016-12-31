package com.device.vk.commands;

import java.util.HashSet;

public class BotCommands {
    private static HashSet<String> botCommands = new HashSet<>();
    static {
        botCommands.add("Бот, переведи");
        botCommands.add("бот, переведи");
        botCommands.add("Бот переведи");
        botCommands.add("бот переведи");
        botCommands.add("Бот, погода в");
    }
    public static HashSet<String> getBotCommands (){
        return botCommands;
    }
}
