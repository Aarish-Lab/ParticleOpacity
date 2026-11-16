package com.darkvenice.particleopacity;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client entry point for ParticleOpacity. */
public final class ParticleOpacity implements ClientModInitializer {
    public static final String MOD_ID = "particleopacity";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ParticleOpacityConfig.load();
        LOGGER.info("ParticleOpacity client configuration loaded.");
    }
}
