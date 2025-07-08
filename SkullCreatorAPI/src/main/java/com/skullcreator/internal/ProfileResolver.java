package com.skullcreator.internal;

import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Skull;

import com.skullcreator.internal.strategy.ProfileApplier;
import com.skullcreator.internal.bootstrap.ReflectionBootstrap;

/**
 * Thin facade: delegates to the chosen {@link ProfileApplier}.
 * All heavy reflection moved to {@link ReflectionBootstrap}/{@link com.skullcreator.internal.strategy.ModernApplier}.
 */
public final class ProfileResolver {

    private ProfileResolver() {}

    private static final ProfileApplier APPLIER = ReflectionBootstrap.bootstrap();

    public static ItemStack item(ItemStack item, String base64) {
        return APPLIER.applyToItem(item, base64);
    }

    public static void block(Skull skull, String base64) {
        APPLIER.applyToBlock(skull, base64);
    }

    /** @return true if the modern reflection path is active. */
    public static boolean ready() {
        return "ModernApplier".equals(APPLIER.getClass().getSimpleName());
    }

    public static void clearCache() {
        if (APPLIER instanceof com.skullcreator.internal.strategy.ModernApplier modern) {
            modern.clearCache();
        }
    }

    public static int getCacheSize() {
        if (APPLIER instanceof com.skullcreator.internal.strategy.ModernApplier modern) {
            return modern.getCacheSize();
        }
        return 0;
    }
}