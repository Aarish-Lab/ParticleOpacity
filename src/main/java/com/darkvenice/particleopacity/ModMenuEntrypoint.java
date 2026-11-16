package com.darkvenice.particleopacity;

import com.darkvenice.particleopacity.config.ParticleOpacityScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ModMenuEntrypoint implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ParticleOpacityScreen(parent);
    }
}
