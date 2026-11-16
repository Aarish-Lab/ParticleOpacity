package com.darkvenice.particleopacity.access;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockParticleSource {
    BlockState particleopacity$getBlockState();
    void particleopacity$setBlockState(BlockState state);
    Block particleopacity$getBlock();
    void particleopacity$setBlock(Block block);
}
