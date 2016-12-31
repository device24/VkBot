package com.device.thread;

import com.device.vk.VkService;
import com.device.vk.VkServiceImpl;

public class VkThread extends Thread {
    public static VkService vkService = new VkServiceImpl();
    @Override
    public void run() {
        while (true){
            vkService.getMessagesUpdate();
        }
    }
}
