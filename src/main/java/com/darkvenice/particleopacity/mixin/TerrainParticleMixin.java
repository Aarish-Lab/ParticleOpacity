package com.darkvenice.particleopacity.mixin;

import com.darkvenice.particleopacity.ParticleOpacityRuntime;
import com.darkvenice.particleopacity.access.BlockParticleSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin implements BlockParticleSource {
    @Unique private BlockState particleopacity$sourceState;
    @Unique private Block particleopacity$sourceBlock;

    @Override
    public BlockState particleopacity$getBlockState() {
        return particleopacity$sourceState;
    }

    @Override
    public void particleopacity$setBlockState(BlockState state) {
        particleopacity$sourceState = state;
    }

    @Override
    public Block particleopacity$getBlock() {
        return particleopacity$sourceBlock;
    }

    @Override
    public void particleopacity$setBlock(Block block) {
        particleopacity$sourceBlock = block;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("TAIL")
    )
    private void particleopacity$capturePrimaryConstructor(ClientLevel level, double x, double y, double z,
                                                            double xd, double yd, double zd, BlockState state,
                                                            CallbackInfo ci) {
        ParticleOpacityRuntime.captureBlockState(this, state);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
            at = @At("TAIL")
    )
    private void particleopacity$capturePositionConstructor(ClientLevel level, double x, double y, double z,
                                                             double xd, double yd, double zd, BlockState state,
                                                             BlockPos pos, CallbackInfo ci) {
        ParticleOpacityRuntime.captureBlockState(this, state);
    }

    @Inject(method = "getLayer", at = @At("HEAD"), cancellable = true)
    private void particleopacity$useTranslucentLayer(CallbackInfoReturnable<SingleQuadParticle.Layer> cir) {
        if (ParticleOpacityRuntime.shouldUseBlockLayer(this)) {
            cir.setReturnValue(SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN);
        }
    }
}
