package dev.shiro8613.kotonohaid;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;

public class Kotonohaid implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LogUtils.getLogger().info("KotonohaID load complete");
    }
}
