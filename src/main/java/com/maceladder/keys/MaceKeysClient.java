package com.maceladder.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Client side of the Mace Ladder server.
 *  - ` (backtick)  -> Gold mace and up: dash forward (3 s cooldown, no hunger cost, not while gliding; the SERVER moves you)
 *  - R             -> Iron mace and up: parry
 *  - Z             -> Amethyst mace and up: sky jump
 * All keys are changeable in Controls. The server (datapack) enforces the rules.
 */
public class MaceKeysClient implements ClientModInitializer {
    private static final String[] MODELS = {
        "normal_mace", "wooden_mace", "stone_mace", "copper_mace", "quartz_mace", "gold_mace", "lapis_mace",
        "iron_mace", "redstone_mace", "diamond_mace", "emerald_mace", "netherite_mace", "amethyst_mace", "god_mace"
    };
    private static final int DASH_TIER = 5, PARRY_TIER = 7, SKY_TIER = 12;
    private static final int DASH_COOLDOWN_TICKS = 60;   // 3 seconds

    private static KeyMapping dashKey, parryKey, skyKey;
    private int dashCooldown = 0;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("macekeys", "abilities"));
        dashKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.macekeys.dash", 96, category));   // 96 = ` (grave accent)
        parryKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.macekeys.parry", InputConstants.KEY_R, category));
        skyKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.macekeys.sky", InputConstants.KEY_Z, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);

        // hide the "Triggered [mace.xxx]" chat messages
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            String s = message.getString();
            return !(s.contains("mace.dash") || s.contains("mace.parry") || s.contains("mace.sky"));
        });
    }

    private void tick(Minecraft mc) {
        if (mc.player == null) return;
        int tier = tier(mc.player);
        if (dashCooldown > 0) dashCooldown--;

        while (dashKey.consumeClick()) {
            if (tier >= DASH_TIER && dashCooldown == 0 && !mc.player.isFallFlying() && !mc.player.hasEffect(MobEffects.SLOWNESS)) {
                dashCooldown = DASH_COOLDOWN_TICKS;
                mc.player.connection.sendCommand("trigger mace.dash");
            }
        }
        while (parryKey.consumeClick()) {
            if (tier >= PARRY_TIER) mc.player.connection.sendCommand("trigger mace.parry");
        }
        while (skyKey.consumeClick()) {
            if (tier >= SKY_TIER) mc.player.connection.sendCommand("trigger mace.sky");
        }
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
