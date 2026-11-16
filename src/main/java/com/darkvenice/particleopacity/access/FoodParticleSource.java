package com.darkvenice.particleopacity.access;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface FoodParticleSource {
    ItemStack particleopacity$getFoodStack();
    void particleopacity$setFoodStack(ItemStack stack);
    Item particleopacity$getFoodItem();
    void particleopacity$setFoodItem(Item item);
}
