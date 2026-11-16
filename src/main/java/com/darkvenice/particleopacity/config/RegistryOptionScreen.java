package com.darkvenice.particleopacity.config;

import com.darkvenice.particleopacity.ParticleOpacityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Searchable registry list for blocks or edible items. */
public final class RegistryOptionScreen extends Screen {
    public enum Kind { BLOCKS, FOODS }

    private final Screen parent;
    private final Kind kind;
    private EditBox searchBox;
    private RegistryToggleList registryList;

    public RegistryOptionScreen(Screen parent, Kind kind) {
        super(Component.literal(kind == Kind.BLOCKS ? "Specific Blocks" : "Specific Foods / Items"));
        this.parent = parent;
        this.kind = kind;
    }

    @Override
    protected void init() {
        int margin = 18;
        int contentWidth = Math.max(180, width - margin * 2);

        searchBox = addRenderableWidget(new EditBox(font, margin, 30, contentWidth, 20,
                Component.literal("Search registry entries")));
        searchBox.setHint(Component.literal("Search by name or ID..."));
        searchBox.setResponder(this::refreshList);

        registryList = new RegistryToggleList(minecraft, width, Math.max(70, height - 112), 58, 25);
        addRenderableWidget(registryList);
        refreshList(searchBox.getValue());

        int y = height - 48;
        int gap = 6;
        int buttonWidth = Math.max(72, (contentWidth - gap * 2) / 3);
        addRenderableWidget(Button.builder(Component.literal("Enable All"), button -> setAll(true))
                .bounds(margin, y, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Disable All"), button -> setAll(false))
                .bounds(margin + buttonWidth + gap, y, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
                .bounds(margin + (buttonWidth + gap) * 2, y, buttonWidth, 20).build());
    }

    private void refreshList(String query) {
        if (registryList != null) registryList.refresh(query);
    }

    private void setAll(boolean enabled) {
        if (kind == Kind.BLOCKS) {
            ParticleOpacityConfig.setAllBlocksEnabled(enabled, ParticleOpacityConfig.getAllBlockIds());
        } else {
            List<Identifier> foodIds = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (new ItemStack(item).has(DataComponents.FOOD)) {
                    foodIds.add(BuiltInRegistries.ITEM.getKey(item));
                }
            }
            ParticleOpacityConfig.setAllFoodsEnabled(enabled, foodIds);
        }
        refreshList(searchBox == null ? "" : searchBox.getValue());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        ParticleOpacityConfig.save();
        minecraft.setScreen(parent);
    }

    private final class RegistryToggleList extends AbstractSelectionList<RegistryToggleEntry> {
        private RegistryToggleList(Minecraft minecraft, int width, int height, int top, int rowHeight) {
            super(minecraft, width - 24, height, top, rowHeight);
            setX(12);
        }

        private void refresh(String query) {
            clearEntries();
            String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
            List<RegistryToggleEntry> entries = new ArrayList<>();

            if (kind == Kind.BLOCKS) {
                for (Block block : BuiltInRegistries.BLOCK) {
                    if (block == Blocks.AIR) continue;
                    Identifier id = BuiltInRegistries.BLOCK.getKey(block);
                    Component displayName = Component.translatable(block.getDescriptionId());
                    if (matches(normalized, displayName.getString(), id.toString())) {
                        entries.add(new RegistryToggleEntry(id, displayName, new ItemStack(block.asItem()), true));
                    }
                }
            } else {
                for (Item item : BuiltInRegistries.ITEM) {
                    ItemStack stack = new ItemStack(item);
                    if (!stack.has(DataComponents.FOOD)) continue;
                    Identifier id = BuiltInRegistries.ITEM.getKey(item);
                    Component displayName = Component.translatable(item.getDescriptionId());
                    if (matches(normalized, displayName.getString(), id.toString())) {
                        entries.add(new RegistryToggleEntry(id, displayName, stack, false));
                    }
                }
            }

            entries.sort(Comparator.comparing(entry -> entry.name.getString(), String.CASE_INSENSITIVE_ORDER));
            for (RegistryToggleEntry entry : entries) addEntry(entry);
        }

        private boolean matches(String query, String name, String id) {
            return query.isEmpty() || name.toLowerCase(Locale.ROOT).contains(query)
                    || id.toLowerCase(Locale.ROOT).contains(query);
        }
    }

    private final class RegistryToggleEntry extends AbstractSelectionList.Entry<RegistryToggleEntry> {
        private final Identifier id;
        private final Component name;
        private final ItemStack icon;
        private final boolean block;

        private RegistryToggleEntry(Identifier id, Component name, ItemStack icon, boolean block) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.block = block;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   boolean hovered, float partialTick) {
            int x = getContentX();
            int y = getContentY();
            if (hovered) graphics.fill(x, y, getContentRight(), y + getContentHeight(), 0x33FFFFFF);
            graphics.item(icon, x + 2, y + 3);
            graphics.text(font, name, x + 24, y + 7, 0xFFFFFF);

            boolean enabled = block ? ParticleOpacityConfig.isBlockEnabled(id) : ParticleOpacityConfig.isFoodEnabled(id);
            int buttonLeft = getContentRight() - 58;
            int buttonColor = enabled ? 0xFF245A32 : 0xFF632B2B;
            graphics.fill(buttonLeft, y + 3, buttonLeft + 54, y + getContentHeight() - 3, buttonColor);
            graphics.centeredText(font, enabled ? "ON" : "OFF", buttonLeft + 27, y + 7, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() == 0 && event.x() >= getContentRight() - 62 && event.x() <= getContentRight()
                    && event.y() >= getContentY() && event.y() <= getContentBottom()) {
                boolean currentlyEnabled = block
                        ? ParticleOpacityConfig.isBlockEnabled(id)
                        : ParticleOpacityConfig.isFoodEnabled(id);
                if (block) ParticleOpacityConfig.setBlockEnabled(id, !currentlyEnabled);
                else ParticleOpacityConfig.setFoodEnabled(id, !currentlyEnabled);
                return true;
            }
            return false;
        }
    }
}
