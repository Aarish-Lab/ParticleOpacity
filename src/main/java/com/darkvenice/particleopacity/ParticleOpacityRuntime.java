package com.darkvenice.particleopacity;

import com.darkvenice.particleopacity.access.BlockParticleSource;
import com.darkvenice.particleopacity.access.FoodParticleSource;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponents;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/** Small render bridge shared by the mixins. No Sodium internals are referenced. */
public final class ParticleOpacityRuntime {
    private static final Map<TextureAtlasSprite, ItemStack> PENDING_FOOD_SPRITES =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private ParticleOpacityRuntime() {}

    public static void captureBlockState(BlockParticleSource particle, BlockState state) {
        particle.particleopacity$setBlockState(state);
        particle.particleopacity$setBlock(state == null ? null : state.getBlock());
    }

    public static void captureFoodSprite(ItemStackTemplate template, TextureAtlasSprite sprite) {
        if (template == null || sprite == null) return;
        try {
            ItemStack sourceStack = template.create();
            if (sourceStack.has(DataComponents.FOOD)) {
                PENDING_FOOD_SPRITES.put(sprite, sourceStack);
            }
        } catch (RuntimeException ignored) {
            // A malformed/custom ItemStackTemplate should not break particle creation.
        }
    }

    public static ItemStack takeFoodStack(TextureAtlasSprite sprite) {
        return PENDING_FOOD_SPRITES.remove(sprite);
    }

    public static Identifier getBlockId(Block block) {
        return block == null ? null : BuiltInRegistries.BLOCK.getKey(block);
    }

    public static Identifier getItemId(Item item) {
        return item == null ? null : BuiltInRegistries.ITEM.getKey(item);
    }

    public static boolean shouldUseBlockLayer(BlockParticleSource source) {
        return ParticleOpacityConfig.shouldTranslucentlyRenderBlock(getBlockId(source.particleopacity$getBlock()));
    }

    public static boolean shouldUseFoodLayer(FoodParticleSource source) {
        return ParticleOpacityConfig.shouldTranslucentlyRenderFood(getItemId(source.particleopacity$getFoodItem()));
    }

    public static float opacityFor(Particle particle) {
        if (particle instanceof TerrainParticle && particle instanceof BlockParticleSource source) {
            return ParticleOpacityConfig.blockMultiplier(getBlockId(source.particleopacity$getBlock()));
        }
        if (particle instanceof BreakingItemParticle && particle instanceof FoodParticleSource source) {
            return ParticleOpacityConfig.foodMultiplier(getItemId(source.particleopacity$getFoodItem()));
        }
        return 1.0f;
    }
}
