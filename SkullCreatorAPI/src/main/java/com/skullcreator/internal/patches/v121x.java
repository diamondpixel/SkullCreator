package com.skullcreator.internal.patches;

import com.skullcreator.internal.PatchesRegistry;
import com.skullcreator.internal.interfaces.SupportedVersion;
import com.skullcreator.internal.interfaces.VersionPatch;
import com.skullcreator.internal.util.Utilities;
import com.skullcreator.internal.util.TexturePropertyWriter;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Patch for Minecraft 1.21.x where Bukkit switched fully to PlayerProfile API.
 */
@SupportedVersion({"1.21."})
public final class v121x implements VersionPatch {

    static { PatchesRegistry.register(new v121x()); }

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    // Use legacy GameProfile injection because Bukkit 1.21 requires signed textures
    private static final Field SKULL_PROFILE_FIELD;
    static {
        Field pf = null;
        try {
            String version = Utilities.getObcVersion();
            Class<?> cms;
            try {
                cms = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftMetaSkull");
            } catch (ClassNotFoundException e) {
                cms = Class.forName("org.bukkit.craftbukkit.inventory.CraftMetaSkull");
            }
            pf = cms.getDeclaredField("profile");
            pf.setAccessible(true);
        } catch (Exception ignored) {}
        SKULL_PROFILE_FIELD = pf;
    }

    @Override
    public ItemStack applyToItem(ItemStack item, String base64) {
        if (!(item.getItemMeta() instanceof SkullMeta) || base64 == null) {
            return item;
        }

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (SKULL_PROFILE_FIELD == null) {
            return item;
        }

        Object gp = getCachedGameProfile(base64);
        if (gp == null) {
            return item;
        }

        // Build object compatible with CraftMetaSkull.profile type
        Object profileObj = gp;
        Class<?> fieldType = SKULL_PROFILE_FIELD.getType();
        if (!fieldType.isAssignableFrom(gp.getClass())) {
            // Attempt to wrap into ResolvableProfile (1.21+)
            try {
                for (java.lang.reflect.Constructor<?> c : fieldType.getDeclaredConstructors()) {
                    Class<?>[] pt = c.getParameterTypes();
                    if (pt.length >= 1 && pt[0].isAssignableFrom(gp.getClass())) {
                        Object[] args = new Object[pt.length];
                        args[0] = gp;
                        for (int i = 1; i < pt.length; i++) {
                            // supply sensible defaults for remaining params
                            args[i] = (pt[i] == boolean.class || pt[i] == Boolean.class) ? Boolean.FALSE : null;
                        }
                        c.setAccessible(true);
                        profileObj = c.newInstance(args);
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        try {
            SKULL_PROFILE_FIELD.set(meta, profileObj);
            item.setItemMeta(meta);
        } catch (Exception e) {}
        return item;
    }

    private Object getCachedGameProfile(String base64) {
        return cache.computeIfAbsent(base64, Utilities::createGameProfile);
    }

    /* ------------------------------------------------------------ */
    /*  Block (Skull) support                                        */
    /* ------------------------------------------------------------ */

    private static volatile Field BLOCK_PROFILE_FIELD;

    @Override
    public void applyToBlock(Skull skull, String base64) {
        if (base64 == null || skull == null) return;

        // locate the profile field lazily
        Field pf = BLOCK_PROFILE_FIELD;
        if (pf == null) {
            for (Field f : skull.getClass().getDeclaredFields()) {
                Class<?> type = f.getType();
                String name = f.getName().toLowerCase();
                if (name.contains("profile") || name.contains("gameprofile")) {
                    f.setAccessible(true);
                    pf = f;
                    break;
                }
            }
            BLOCK_PROFILE_FIELD = pf; // may be null if not found
        }

        if (pf == null) return;

        Object gp = getCachedGameProfile(base64);
        if (gp == null) return;

        Object profileObj = gp;
        Class<?> fieldType = pf.getType();
        if (!fieldType.isAssignableFrom(gp.getClass())) {
            try {
                for (java.lang.reflect.Constructor<?> c : fieldType.getDeclaredConstructors()) {
                    Class<?>[] pt = c.getParameterTypes();
                    if (pt.length >= 1 && pt[0].isAssignableFrom(gp.getClass())) {
                        Object[] args = new Object[pt.length];
                        args[0] = gp;
                        for (int i = 1; i < pt.length; i++) {
                            args[i] = (pt[i] == boolean.class || pt[i] == Boolean.class) ? Boolean.FALSE : null;
                        }
                        c.setAccessible(true);
                        profileObj = c.newInstance(args);
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        try {
            pf.set(skull, profileObj);
            // ensure block state updates
            skull.update(true, true);
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
}
