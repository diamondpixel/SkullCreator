package com.skullcreator.internal;

import com.skullcreator.internal.bootstrap.PatchBootstrap;
import com.skullcreator.internal.interfaces.VersionPatch;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;

/**
 * Facade used by public API to apply custom textures either to an ItemStack or a
 * placed Skull block state. Internally it delegates to the version-specific
 * {@link VersionPatch} selected by {@link PatchBootstrap}.
 */
public final class TextureApplier {

    private TextureApplier() {}

    /* ------------------------------------------------------------ */
    /*  Bootstrap                                                    */
    /* ------------------------------------------------------------ */

    private static VersionPatch PATCH;

    static {
        PatchBootstrap.bootstrap();
        PATCH = PatchBootstrap.active();
    }

    /* ------------------------------------------------------------ */
    /*  Public API                                                   */
    /* ------------------------------------------------------------ */

    private static volatile boolean warned;

    public static ItemStack item(ItemStack item, String base64) {
        if (PATCH == null) {
            logOnce();
            return item;
        }
        return PATCH.applyToItem(item, base64);
    }

    public static void block(Skull skull, String base64) {
        if (PATCH == null) {
            logOnce();
            return;
        }
        PATCH.applyToBlock(skull, base64);
    }

    private static void logOnce() {
        if (!warned) {
            org.bukkit.Bukkit.getLogger().warning("[SkullCreator] No compatible VersionPatch for this server; heads will stay vanilla.");
            warned = true;
        }
    }

    /** @return true if modern (patch-based) path is active. */
    public static boolean ready() {
        return PATCH != null;
    }

    public static void clearCache() {
        if (PATCH != null) PATCH.clearCache();
    }

    public static int getCacheSize() {
        return PATCH != null ? PATCH.getCacheSize() : 0;
    }
}
