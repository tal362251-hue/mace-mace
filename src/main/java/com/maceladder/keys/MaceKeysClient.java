package com.maceladder.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Client side of the Mace Ladder server.
 *  - Sky key (default Z, changeable in Controls) -> "/trigger mace.sky"   (Amethyst mace and up)
 *  - Dash key (default `, changeable in Controls) -> "/trigger mace.dash"  (Gold mace and up)
 *  - Pressing jump again in the air            -> "/trigger mace.djump" (Diamond mace and up)
 * The server (datapack) decides if it is allowed and does the actual jump / teleport.
 */
public class MaceKeysClient implements ClientModInitializer {
    private static final String[] MODELS = {
        "normal_mace", "wooden_mace", "stone_mace", "copper_mace", "quartz_mace", "gold_mace", "lapis_mace",
        "iron_mace", "redstone_mace", "diamond_mace", "emerald_mace", "netherite_mace", "amethyst_mace", "god_mace"
    };
    private static final int DASH_TIER = 5, DOUBLE_JUMP_TIER = 9, SKY_TIER = 12;
    /** The server only allows a double jump after 3 ticks in the air, so wait for that here too. */
    private static final int MIN_AIR_TICKS = 4;

    private static KeyMapping skyKey;
    private static KeyMapping dashKey;
    private boolean jumpWasDown = false;
    private boolean doubleJumpUsed = false;
    private int airTicks = 0;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("macekeys", "abilities"));
        skyKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.macekeys.sky", InputConstants.KEY_Z, category));
        dashKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.macekeys.dash", InputConstants.KEY_GRAVE, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft mc) {
        if (mc.player == null) return;
        int tier = tier(mc.player);

        while (skyKey.consumeClick()) {
            if (tier >= SKY_TIER) mc.player.connection.sendCommand("trigger mace.sky");
        }
        while (dashKey.consumeClick()) {
            if (tier >= DASH_TIER) mc.player.connection.sendCommand("trigger mace.dash");
        }

        boolean jumpDown = mc.options.keyJump.isDown();
        boolean onGround = mc.player.onGround();
        if (onGround) {
            doubleJumpUsed = false;
            airTicks = 0;
        } else {
            airTicks++;
        }
        if (jumpDown && !jumpWasDown && !onGround && !doubleJumpUsed && tier >= DOUBLE_JUMP_TIER
                && airTicks >= MIN_AIR_TICKS && mc.screen == null && !mc.player.isInWater()
                && !mc.player.getAbilities().flying && !mc.player.isFallFlying()) {
            mc.player.connection.sendCommand("trigger mace.djump");
            doubleJumpUsed = true;
        }
        jumpWasDown = jumpDown;
    }

    /** Highest mace tier in the inventory (-1 if none), read from the item model the server gives. */
    private static int tier(Player player) {
        int best = -1;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            Identifier model = stack.get(DataComponents.ITEM_MODEL);
            if (model == null || !"mace".equals(model.getNamespace())) continue;
            for (int t = 0; t < MODELS.length; t++) {
                if (MODELS[t].equals(model.getPath())) best = Math.max(best, t);
            }
        }
        return best;
    }
}
