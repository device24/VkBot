package com.device.vk;

import com.device.translate.Translate;
import com.device.vk.commands.BotCommands;
import com.device.vk.model.dialog.Dialog;
import com.device.vk.model.dialog.DialogResponse;
import com.device.vk.model.dialog.Message;
import com.device.vk.model.dialog.MessageResponse;
import com.device.vk.model.user.User;
import com.device.vk.model.user.UserResponse;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

@Transactional
@Service
public class VkServiceImpl implements VkService {


    private static final Logger log = Logger.getLogger(VkServiceImpl.class);


    private static String PEER_ID;

    private Long offset = 0L;
    private Long count = 0L;
    private Long lastMessageId = 0L;

    private Integer rev = 0;

    private Translate translate = new Translate();

    private static String ACCESS_TOKEN = "";
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("src/main/resources/config.properties"));
            ACCESS_TOKEN = properties.getProperty("vk.access.token");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getSendUrl(String message, Long id_user) {
        return "https://api.vk.com/method/messages.send?" +
                "random_id=" + new Random().nextInt(10000) +
                "&peer_id=" + id_user +
                "&message=" + message +
                "&notification=0" +
                "&v=5.60" +
                "&access_token=" + ACCESS_TOKEN;

    }

    public String getReqUrl() {
        return "https://api.vk.com/method/messages.getDialogs?" +
                "offset=" + this.offset +
                "&count=" + this.count +
                "&unread=1" +
                "&v=5.60" +
                "&access_token=" + ACCESS_TOKEN;
    }

    public String getUserUrl(Long id) {

        return "https://api.vk.com/method/users.get?" +
                "user_ids=" + id +
                "&name_case=gen" +
                "&v=5.60" +
                "&access_token=" + ACCESS_TOKEN;

    }

    public String getMarkAsReadUrl(Long messageId, Long user_id) {
        return "https://api.vk.com/method/messages.markAsRead?" +
                "message_ids=" + messageId +
                "&peer_id=" + user_id +
                "&v=5.60" +
                "&access_token=" + ACCESS_TOKEN;
    }

    public void sendMessage(String message, Long id_user) {

        new RestTemplate().getForEntity(getSendUrl(message, id_user), String.class);

    }

    public String getNameAndSurname(Long id) {
        User user = new RestTemplate().getForObject(getUserUrl(id), UserResponse.class).getResponse().get(0);
        return user.getFirstName() + " " + user.getLastName();


    }

    public void markAsRead(Long messageId, Long user_id) {
        new RestTemplate().getForEntity(getMarkAsReadUrl(messageId, user_id), String.class);

    }

    @Scheduled(fixedRate = 700)
    @Override
    public void getMessagesUpdate() {
        this.rev = 0;
        this.count = 50L;
        String lang1;
        String lang2;
        String text;

        Dialog response = new RestTemplate().getForObject(getReqUrl(), DialogResponse.class).getResponse();

        if (response != null) {
            List<MessageResponse> messages = response.getMessages()
                    .stream()
                    .filter(message -> message.getMessage().getReadState() != 1).collect(Collectors.toList());

            for (int i = 0; i < messages.size(); i++) {
                Message receivedMsg = messages.get(i).getMessage();
                for (String com : BotCommands.getBotCommands()) {
                    if (receivedMsg.getBody().startsWith(com)) {
                        lang1 = receivedMsg.getBody().substring(16).split(" ")[0];
                        lang2 = receivedMsg.getBody().substring(16).split(" ")[2];
                        text = receivedMsg.getBody().substring(16 + lang1.length() + lang2.length() + " на  ".length());

                        log.info("Bot command found.");
                        switch (com) {
                            case "Бот, переведи":
                                sendMessage(translate.getTranslateText(text, lang1, lang2), receivedMsg.getUserId());
                                break;
                            case "Бот переведи":
                                sendMessage(translate.getTranslateText(text, receivedMsg.getBody().substring(15).split(" ")[0], lang2), receivedMsg.getUserId());
                                break;
                            case "бот, переведи":
                                sendMessage(translate.getTranslateText(text, lang1, lang2), receivedMsg.getUserId());
                                break;
                            case "бот переведи":
                                sendMessage(translate.getTranslateText(text, receivedMsg.getBody().substring(15).split(" ")[0], lang2), receivedMsg.getUserId());
                                break;
                            case "Бот, погода в":
                                break;
                        }
                        log.info("Bot successfully respond to user.");
                        break;


                    }
                }
                this.lastMessageId = receivedMsg.getId();
                markAsRead(receivedMsg.getId(), receivedMsg.getUserId());
            }
        }
    }

    @Override
    public void dumpAllMessages() {

    }


}
