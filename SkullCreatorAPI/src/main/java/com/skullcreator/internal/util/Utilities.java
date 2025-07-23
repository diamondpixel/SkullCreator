package com.skullcreator.internal.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Shared helper utilities for VersionPatch implementations.
 */
public final class Utilities {
    private Utilities() {}

    /* ------------------------------------------------------------ */
    /*  Public helpers                                               */
    /* ------------------------------------------------------------ */

    /**
     * Deterministically hash a Base64 string to a UUID. Identical to the method previously
     * embedded in patch classes.
     */
    public static UUID deterministicUUID(String base64) {
        long hash = base64.hashCode();
        return new UUID(hash, hash);
    }

    /**
     * Returns the CraftBukkit version segment, e.g. "v1_8_R3" or "v1_20_R2".
     * Useful for building fully-qualified CraftBukkit class names at runtime.
     */
    public static String getObcVersion() {
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = pkg.split("\\.");
        return parts.length > 3 ? parts[3] : "";
    }

    /**
     * Creates a Mojang-authlib GameProfile (or Bukkit PlayerProfile) with the supplied Base64 texture.
     * The return type is {@code Object} to avoid forcing compile-time dependency on authlib classes.
     * Reflection lookups are cached for performance.
     */
    public static Object createGameProfile(String base64) {
        if (base64 == null) return null;
        try {
            ReflectionCache c = ReflectionCache.INSTANCE.get();
            UUID uuid = deterministicUUID(base64);
            Object profile = c.gameProfileCtor.newInstance(uuid, "");
            TexturePropertyWriter.injectTexture(profile, base64);
            return profile;
        } catch (Throwable t) {
            return null;
        }
    }

    /* ------------------------------------------------------------ */
    /*  Internal reflection cache                                    */
    /* ------------------------------------------------------------ */

    private static final class ReflectionCache {
        private static final AtomicReference<ReflectionCache> INSTANCE = new AtomicReference<>();

        static {
            INSTANCE.set(new ReflectionCache());
        }

        private final Constructor<?> gameProfileCtor;

        private ReflectionCache() {
            Constructor<?> gpc = null;
            try {
                Class<?> gameProfileCls = Class.forName("com.mojang.authlib.GameProfile");
                gpc = gameProfileCls.getConstructor(UUID.class, String.class);
            } catch (Exception ignored) {}
            this.gameProfileCtor = gpc;
        }
    }
}
