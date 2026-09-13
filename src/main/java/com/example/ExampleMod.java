package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ExampleMod implements ModInitializer {

    private static final Set<UUID> FRIENDLY_MOBS = new HashSet<>();
    private static final Map<String, String> PROMPTS = new HashMap<>();

    static {
        PROMPTS.put("zombie", "Ты — агрессивный зомби из Minecraft. Материшься, хочешь есть мозги. Если игрок предложит хорошую сделку, напиши в конце [FRIENDLY: TRUE], иначе [FRIENDLY: FALSE].");
        PROMPTS.put("creeper", "Ты — взрывной нервный крипер. Боишься кошек, используешь мат. Если тебя успокоят, напиши в конце [FRIENDLY: TRUE], иначе [FRIENDLY: FALSE].");
        PROMPTS.put("skeleton", "Ты — сноб-скелет, гордишься меткостью, троллишь игрока. Напиши в конце [FRIENDLY: TRUE] или [FRIENDLY: FALSE].");
        PROMPTS.put("default", "Ты — враждебный моб из Minecraft. Разговариваешь дерзко, используешь мат. В конце ответь [FRIENDLY: TRUE] или [FRIENDLY: FALSE].");
    }

    @Override
    public void onInitialize() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && entity instanceof LivingEntity mob) {
                if (mob instanceof MobEntity mobEntity) {
                    mobEntity.setTarget(null);
                }
                startDialogue(player, mob, mob.getType().getName().getString());
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    private void startDialogue(PlayerEntity player, LivingEntity mob, String mobType) {
        new Thread(() -> {
            try {
                player.sendMessage(Text.of("§e[Микрофон] Запись..."), false);
                byte[] audio = recordWav(3000);
                String userMessage = "Привет, не бей меня!";

                String prompt = PROMPTS.getOrDefault(mobType.toLowerCase(), PROMPTS.get("default"));
                String response = askAI(prompt, userMessage);

                boolean becameFriendly = response.contains("[FRIENDLY: TRUE]");
                String cleanResponse = response.replaceAll("\\[FRIENDLY: (TRUE|FALSE)\\]", "").trim();

                player.sendMessage(Text.of("§c" + mobType + ": §f" + cleanResponse), false);
                speak(cleanResponse);

                if (becameFriendly) {
                    FRIENDLY_MOBS.add(mob.getUuid());
                    if (mob instanceof MobEntity mobEntity) {
                        mobEntity.setTarget(null);
                    }
                    player.sendMessage(Text.of("§aМоб больше не нападет!"), false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String askAI(String systemPrompt, String userMessage) {
        try {
            String cleanPrompt = systemPrompt.replace("\"", "\\\"").replace("\n", " ");
            String cleanMessage = userMessage.replace("\"", "\\\"").replace("\n", " ");
            String jsonPayload = String.format("""
                {
                    "messages": [
                        {"role": "system", "content": "%s"},
                        {"role": "user", "content": "%s"}
                    ],
                    "model": "openai"
                }
                """, cleanPrompt, cleanMessage);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://text.pollinations.ai/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().trim();
        } catch (Exception e) {
            return "Чё надо? Я занят! [FRIENDLY: FALSE]";
        }
    }

    private void speak(String text) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=ru&q=" + encodedText;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(URI.create(url).toURL());
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] recordWav(int durationMs) throws Exception {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        ByteArrayOutputStream pcmOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        long endTime = System.currentTimeMillis() + durationMs;

        while (System.currentTimeMillis() < endTime) {
            int read = line.read(buffer, 0, buffer.length);
            if (read > 0) pcmOut.write(buffer, 0, read);
        }

        line.stop();
        line.close();

        byte[] pcmData = pcmOut.toByteArray();
        ByteArrayOutputStream wavOut = new ByteArrayOutputStream();
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(pcmData), format, pcmData.length / format.getFrameSize());
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavOut);

        return wavOut.toByteArray();
    }
			}
