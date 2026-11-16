package com.darkvenice.particleopacity.mixin;

import com.darkvenice.particleopacity.ParticleOpacityRuntime;
import com.darkvenice.particleopacity.access.FoodParticleSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreakingItemParticle.class)
public abstract class BreakingItemParticleMixin implements FoodParticleSource {
    @Unique private Item particleopacity$foodItem;
    @Unique private ItemStack particleopacity$foodStack;

    @Override
    public ItemStack particleopacity$getFoodStack() {
        return particleopacity$foodStack;
    }

    @Override
    public void particleopacity$setFoodStack(ItemStack stack) {
        particleopacity$foodStack = stack;
        particleopacity$foodItem = stack == null || stack.isEmpty() ? null : stack.getItem();
    }

    @Override
    public Item particleopacity$getFoodItem() {
        return particleopacity$foodItem;
    }

    @Override
    public void particleopacity$setFoodItem(Item item) {
        particleopacity$foodItem = item;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
            at = @At("TAIL"), require = 0
    )
    private void particleopacity$captureFullConstructor(ClientLevel level, double x, double y, double z,
                                                         double xd, double yd, double zd, TextureAtlasSprite sprite,
                                                         CallbackInfo ci) {
        particleopacity$setFoodStack(ParticleOpacityRuntime.takeFoodStack(sprite));
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDLnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
            at = @At("TAIL"), require = 0
    )
    private void particleopacity$captureSimpleConstructor(ClientLevel level, double x, double y, double z,
                                                           TextureAtlasSprite sprite, CallbackInfo ci) {
        particleopacity$setFoodStack(ParticleOpacityRuntime.takeFoodStack(sprite));
    }

    @Inject(method = "getLayer", at = @At("HEAD"), cancellable = true)
    private void particleopacity$useTranslucentLayer(CallbackInfoReturnable<SingleQuadParticle.Layer> cir) {
        if (ParticleOpacityRuntime.shouldUseFoodLayer(this)) {
            cir.setReturnValue(SingleQuadParticle.Layer.TRANSLUCENT_ITEMS);
        }
    }
}
