package com.darkvenice.particleopacity.mixin;

import com.darkvenice.particleopacity.ParticleOpacityRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreakingItemParticle.ItemParticleProvider.class)
public abstract class BreakingItemParticleProviderMixin {
    @Inject(method = "getSprite", at = @At("RETURN"), require = 0)
    private void particleopacity$captureFoodSource(ItemStackTemplate stack, ClientLevel level, RandomSource random,
                                                   CallbackInfoReturnable<TextureAtlasSprite> cir) {
        ParticleOpacityRuntime.captureFoodSprite(stack, cir.getReturnValue());
    }
}
