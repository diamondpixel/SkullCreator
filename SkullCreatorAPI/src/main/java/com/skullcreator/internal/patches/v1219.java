package com.skullcreator.internal.patches;

import com.skullcreator.internal.PatchesRegistry;
import com.skullcreator.internal.interfaces.SupportedVersion;
import com.skullcreator.internal.interfaces.VersionPatch;
import com.skullcreator.internal.util.Utilities;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Patch for Minecraft 1.21.9+ where Bukkit switched fully to PlayerProfile API.
 * Uses Bukkit's official PlayerProfile API for custom textures.
 */
@SupportedVersion({
        "1.21.9+"
})
public final class v1219 implements VersionPatch {

    static {
        PatchesRegistry.register(new v1219());
    }

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public ItemStack applyToItem(ItemStack item, String base64) {
        if (!(item.getItemMeta() instanceof SkullMeta) || base64 == null) {
            return item;
        }

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        
        Object playerProfile = getCachedPlayerProfile(base64);
        if (playerProfile == null) {
            return item;
        }

        try {
            // Use SkullMeta.setOwnerProfile() API via interface types
            Class<?> skullMetaInterface = Class.forName("org.bukkit.inventory.meta.SkullMeta");
            Class<?> playerProfileInterface = Class.forName("org.bukkit.profile.PlayerProfile");
            java.lang.reflect.Method setOwnerProfileMethod = skullMetaInterface.getMethod("setOwnerProfile", playerProfileInterface);
            setOwnerProfileMethod.setAccessible(true);
            setOwnerProfileMethod.invoke(meta, playerProfile);
            item.setItemMeta(meta);
        } catch (Exception e) {
            // Failed to set profile
        }
        return item;
    }

    private Object getCachedPlayerProfile(String base64) {
        return cache.computeIfAbsent(base64, Utilities::createPlayerProfile);
    }

    /* ------------------------------------------------------------ */
    /*  Block (Skull) support                                        */
    /* ------------------------------------------------------------ */

    @Override
    public void applyToBlock(Skull skull, String base64) {
        if (skull == null || base64 == null) {
            return;
        }

        Object playerProfile = getCachedPlayerProfile(base64);
        if (playerProfile == null) {
            return;
        }

        try {
            // Use Skull.setOwnerProfile() API via interface types
            Class<?> skullInterface = Class.forName("org.bukkit.block.Skull");
            Class<?> playerProfileInterface = Class.forName("org.bukkit.profile.PlayerProfile");
            java.lang.reflect.Method setOwnerProfileMethod = skullInterface.getMethod("setOwnerProfile", playerProfileInterface);
            setOwnerProfileMethod.setAccessible(true);
            setOwnerProfileMethod.invoke(skull, playerProfile);
            
            // Update the block state
            skull.update(true, false);
        } catch (Exception e) {
            // Failed to update block
        }
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public int getCacheSize() {
        return cache.size();
    }
}
