package com.darkvenice.particleopacity.mixin;

import com.darkvenice.particleopacity.ParticleOpacityRuntime;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.particle.SingleQuadParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies opacity only while the common particle render state is extracted.
 * This is renderer-neutral: it does not inject into Sodium or vertex-writer code.
 */
@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin {
    @Shadow protected float alpha;

    @Unique private float particleopacity$oldAlpha;
    @Unique private boolean particleopacity$restoreAlpha;

    @Inject(method = "extract", at = @At("HEAD"))
    private void particleopacity$applyOpacity(QuadParticleRenderState renderState, Camera camera,
                                              float partialTickTime, CallbackInfo ci) {
        float multiplier = ParticleOpacityRuntime.opacityFor((SingleQuadParticle) (Object) this);
        if (multiplier < 0.999999f) {
            particleopacity$oldAlpha = alpha;
            alpha *= multiplier;
            particleopacity$restoreAlpha = true;
        } else {
            particleopacity$restoreAlpha = false;
        }
    }

    @Inject(method = "extract", at = @At("RETURN"))
    private void particleopacity$restoreOpacity(QuadParticleRenderState renderState, Camera camera,
                                                float partialTickTime, CallbackInfo ci) {
        if (particleopacity$restoreAlpha) {
            alpha = particleopacity$oldAlpha;
            particleopacity$restoreAlpha = false;
        }
    }
}
