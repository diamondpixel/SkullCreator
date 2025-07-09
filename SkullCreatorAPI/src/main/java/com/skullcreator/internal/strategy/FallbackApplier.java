package com.skullcreator.internal.strategy;

import com.skullcreator.internal.util.TexturePropertyWriter;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Public-API based implementation used when direct reflection into CraftBukkit
 * internals is not available (or failed). Relies on Bukkit/ Paper profile
 * classes only, so it should work across versions without breaking.
 */
public class FallbackApplier implements ProfileApplier {

    @Override
    public ItemStack applyToItem(ItemStack item, String base64) {
        if (!(item.getItemMeta() instanceof SkullMeta)) return item;
        try {
            // Attempt to create PlayerProfile via reflection (>=1.19)
            Object playerProfile;
            try {
                java.lang.reflect.Method create = Bukkit.class.getMethod("createProfile", UUID.class, String.class);
                playerProfile = create.invoke(null, UUID.randomUUID(), null);
            } catch (NoSuchMethodException nf) {
                // Older versions: use GameProfile directly
                Class<?> gp = Class.forName("com.mojang.authlib.GameProfile");
                playerProfile = gp.getConstructor(UUID.class, String.class).newInstance(UUID.randomUUID(), "");
            }
            TexturePropertyWriter.injectTexture(playerProfile, base64);

            SkullMeta meta = (SkullMeta) item.getItemMeta();
            boolean applied = false;
            try {
                java.lang.reflect.Method ownerM = meta.getClass().getMethod("setOwnerProfile", playerProfile.getClass());
                ownerM.invoke(meta, playerProfile);
                applied = true;
            } catch (NoSuchMethodException ignored) {}
            if (!applied) {
                try {
                    java.lang.reflect.Method m = meta.getClass().getMethod("setPlayerProfile", playerProfile.getClass());
                    m.invoke(meta, playerProfile);
                    applied = true;
                } catch (Exception e) {
                    try {
                        java.lang.reflect.Field prof = meta.getClass().getDeclaredField("profile");
                        prof.setAccessible(true);
                        prof.set(meta, playerProfile);
                        applied = true;
                    } catch (Exception ignored) {}
                }
            }
            item.setItemMeta(meta);
        } catch (Exception ignored) {}
        return item;
    }

    @Override
    public void applyToBlock(Skull skull, String base64) {
        try {
            // Preferred: Paper/Bukkit API (1.19+)
            Method create = Bukkit.class.getMethod("createProfile", UUID.class, String.class);
            Object playerProfile = create.invoke(null, UUID.randomUUID(), null);
            TexturePropertyWriter.injectTexture(playerProfile, base64);

            // Attempt API setter first
            for (Method m : skull.getClass().getMethods()) {
                if (m.getName().equals("setPlayerProfile") && m.getParameterCount() == 1) {
                    m.invoke(skull, playerProfile);
                    return;
                }
            }
        } catch (NoSuchMethodException ignored) {
            // Older versions (<1.19) – fall through to legacy reflection
        } catch (Exception ignored) {}

        // Legacy path (1.8–1.12): inject GameProfile field via reflection
        try {
            Class<?> gameProfileCls = Class.forName("com.mojang.authlib.GameProfile");
            Object gp = gameProfileCls.getConstructor(UUID.class, String.class).newInstance(UUID.randomUUID(), "");
            TexturePropertyWriter.injectTexture(gp, base64);

            // Find private field holding the profile
            java.lang.reflect.Field profileField = null;
            for (java.lang.reflect.Field f : skull.getClass().getDeclaredFields()) {
                if (gameProfileCls.isAssignableFrom(f.getType())) { profileField = f; break; }
            }
            if (profileField != null) {
                profileField.setAccessible(true);
                profileField.set(skull, gp);
            }
            try {
                Class<?> skullType = Class.forName("org.bukkit.SkullType");
                Object playerType = Enum.valueOf((Class) skullType, "PLAYER");
                Method setType = skull.getClass().getMethod("setSkullType", skullType);
                setType.invoke(skull, playerType);
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }
}
