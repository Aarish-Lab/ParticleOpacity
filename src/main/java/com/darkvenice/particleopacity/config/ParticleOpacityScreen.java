package com.darkvenice.particleopacity.config;

import com.darkvenice.particleopacity.ParticleOpacityConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Main Mod Menu configuration page, using vanilla widgets only. */
public final class ParticleOpacityScreen extends Screen {
    private final Screen parent;

    public ParticleOpacityScreen(Screen parent) {
        super(Component.literal("ParticleOpacity"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(340, this.width - 32);
        int x = (this.width - buttonWidth) / 2;
        int halfWidth = (buttonWidth - 8) / 2;
        int leftX = x;
        int rightX = x + halfWidth + 8;
        int y = 54;

        Button blocksToggle = Button.builder(blocksLabel(), button -> {
            ParticleOpacityConfig.setBlocksEnabled(!ParticleOpacityConfig.blocksEnabled());
            button.setMessage(blocksLabel());
        }).bounds(x, y, buttonWidth, 20).build();
        addRenderableWidget(blocksToggle);

        addRenderableWidget(new PercentSlider(x, y + 25, buttonWidth, 20,
                "Block opacity", ParticleOpacityConfig.blockOpacity(), ParticleOpacityConfig::setBlockOpacity));

        y += 57;
        Button foodsToggle = Button.builder(foodsLabel(), button -> {
            ParticleOpacityConfig.setFoodsEnabled(!ParticleOpacityConfig.foodsEnabled());
            button.setMessage(foodsLabel());
        }).bounds(x, y, buttonWidth, 20).build();
        addRenderableWidget(foodsToggle);

        addRenderableWidget(new PercentSlider(x, y + 25, buttonWidth, 20,
                "Food / item opacity", ParticleOpacityConfig.foodOpacity(), ParticleOpacityConfig::setFoodOpacity));

        y += 58;
        addRenderableWidget(Button.builder(Component.literal("Specific Blocks"), button ->
                this.minecraft.setScreen(new RegistryOptionScreen(this, RegistryOptionScreen.Kind.BLOCKS)))
                .bounds(leftX, y, halfWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Specific Foods / Items"), button ->
                this.minecraft.setScreen(new RegistryOptionScreen(this, RegistryOptionScreen.Kind.FOODS)))
                .bounds(rightX, y, halfWidth, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(x, y + 30, buttonWidth, 20).build());
    }

    private static Component blocksLabel() {
        return Component.literal("Blocks: " + (ParticleOpacityConfig.blocksEnabled() ? "ON" : "OFF"));
    }

    private static Component foodsLabel() {
        return Component.literal("Foods / Items: " + (ParticleOpacityConfig.foodsEnabled() ? "ON" : "OFF"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 22, 0xFFFFFF);
        graphics.centeredText(font, "Transparency is applied only to matching particles.", width / 2, 38, 0xA0A0A0);
    }

    @Override
    public void onClose() {
        ParticleOpacityConfig.save();
        this.minecraft.setScreen(parent);
    }

    private static final class PercentSlider extends AbstractSliderButton {
        private final String label;
        private final java.util.function.IntConsumer onChange;

        private PercentSlider(int x, int y, int width, int height, String label, int initialPercent,
                              java.util.function.IntConsumer onChange) {
            super(x, y, width, height, Component.empty(), Math.max(0, Math.min(100, initialPercent)) / 100.0);
            this.label = label;
            this.onChange = onChange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + Math.round(value * 100.0) + "%"));
        }

        @Override
        protected void applyValue() {
            onChange.accept((int) Math.round(value * 100.0));
        }
    }
}
