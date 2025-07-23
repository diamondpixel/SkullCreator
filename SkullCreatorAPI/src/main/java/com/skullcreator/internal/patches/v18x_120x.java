package com.skullcreator.internal.patches;

import com.skullcreator.internal.PatchesRegistry;
import com.skullcreator.internal.interfaces.SupportedVersion;
import com.skullcreator.internal.interfaces.VersionPatch;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.skullcreator.internal.util.Utilities;

/**
 * Patch for Minecraft 1.8.0 → 1.20.6 .
 * <p>
 * These versions share identical reflection paths for skull texture application.
 * Uses legacy material names and CraftBukkit package structure.
 */
@SupportedVersion({
        "1.8.", "1.9.", "1.10.", "1.11.", "1.12.", "1.13.",
        "1.14.", "1.15.", "1.16.", "1.17.", "1.18.", "1.19.", "1.20."
})
public final class v18x_120x implements VersionPatch {

    static { PatchesRegistry.register(new v18x_120x()); }

    // ---------- reflection state ----------
    private final Field profileField;
    private final Constructor<?> gameProfileCtor;
    private final Constructor<?> propertyCtor;
    private final Field propertiesField;
    private final Method putMethod;
    
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    public v18x_120x() {
        Field pf = null;
        Constructor<?> gpc = null;
        Constructor<?> pc = null;
        Field prf = null;
        Method pm = null;
        
        try {
            // ---------- resolve Mojang Authlib ----------
            Class<?> gameProfileCls = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyCls = Class.forName("com.mojang.authlib.properties.Property");

            gpc = gameProfileCls.getConstructor(UUID.class, String.class);
            pc = propertyCls.getConstructor(String.class, String.class);

            prf = gameProfileCls.getDeclaredField("properties");
            prf.setAccessible(true);

            Object propsObj = prf.get(gpc.newInstance(UUID.randomUUID(), "dummy"));
            pm = propsObj.getClass().getMethod("put", Object.class, Object.class);

            // ---------- CraftMetaSkull.profile ----------
            String version = Utilities.getObcVersion();
            Class<?> craftMetaSkullCls;
            try {
                craftMetaSkullCls = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftMetaSkull");
            } catch (ClassNotFoundException e) {
                craftMetaSkullCls = Class.forName("org.bukkit.craftbukkit.inventory.CraftMetaSkull");
            }
            pf = craftMetaSkullCls.getDeclaredField("profile");
            pf.setAccessible(true);

        } catch (Exception ex) {
            Bukkit.getLogger().warning("SkullCreator: Patch180_1132 reflection init failed → " + ex);
        }
        
        this.profileField = pf;
        this.gameProfileCtor = gpc;
        this.propertyCtor = pc;
        this.propertiesField = prf;
        this.putMethod = pm;
    }

    @Override
    public ItemStack applyToItem(ItemStack item, String base64) {
        if (!(item.getItemMeta() instanceof SkullMeta)) return item;

        if (profileField == null || gameProfileCtor == null) {
            return item; // reflection failed, return unchanged
        }

        try {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            Object gameProfile = getCachedGameProfile(base64);
            if (gameProfile == null) return item;

            profileField.set(meta, gameProfile);
            item.setItemMeta(meta);
        } catch (Exception ex) {
            // reflection failed, return unchanged
        }
        return item;
    }

    @Override
    public void applyToBlock(Skull skull, String base64) {
        if (gameProfileCtor == null) {
            return; // critical reflection fail
        }
        try {
            boolean typeSet = false;
            Object gameProfile = getCachedGameProfile(base64);
            if (gameProfile == null) return;

            // Ensure block is marked as PLAYER before profile injection (important for 1.8)
            try {
                Class<?> skullTypeCls = Class.forName("org.bukkit.SkullType");
                Object playerType = Enum.valueOf((Class) skullTypeCls, "PLAYER");
                java.lang.reflect.Method setType = skull.getClass().getMethod("setSkullType", skullTypeCls);
                setType.invoke(skull, playerType);
                typeSet = true;
            } catch (Exception ignored) {}

            // Direct field injection (only confirmed working path for 1.8.x)
            try {
                Class<?> gpClass = gameProfile.getClass();
                Field fProfile = null;
                for (Field f : skull.getClass().getDeclaredFields()) {
                    if (gpClass.isAssignableFrom(f.getType())) { fProfile = f; break; }
                }
                if (fProfile != null) {
                    fProfile.setAccessible(true);
                    fProfile.set(skull, gameProfile);
                }
            } catch (Exception ignored) {}

            // Push changes to world (older APIs need explicit state save)
            try {
                skull.update(true, true);
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public int getCacheSize() {
        return cache.size();
    }

    // ------------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------------
    private Object getCachedGameProfile(String base64) {
        return cache.computeIfAbsent(base64, this::createGameProfile);
    }

    private Object createGameProfile(String base64) {
        return Utilities.createGameProfile(base64);
    }
}
