package com.skullcreator.internal.bootstrap;

import com.skullcreator.internal.strategy.FallbackApplier;
import com.skullcreator.internal.strategy.ModernApplier;
import com.skullcreator.internal.strategy.ProfileApplier;
import java.lang.reflect.*;
import org.bukkit.Bukkit;

/**
 * Central entry for reflection initialisation. Ensures we perform heavy
 * bootstrap once and pick the best {@link ProfileApplier} implementation.
 */
public final class ReflectionBootstrap {

    private ReflectionBootstrap() {
    }

    public static ProfileApplier bootstrap() {
        ReflectionContext ctx;
        try {
            ctx = buildContextFromScratch();
        } catch (Exception ex) {
            Bukkit.getLogger().warning("SkullCreator: Reflection bootstrap failed -> " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            ctx = null;
        }

        if (ctx != null) {
            return new ModernApplier(ctx);
        }

        return new FallbackApplier();
    }

    /**
     * Resolves CraftBukkit / Mojang classes from scratch and returns a fully
     * populated ReflectionContext.  Throws if *any* required element is missing.
     */
    private static ReflectionContext buildContextFromScratch() throws Exception {

        // ---------- resolve Mojang Authlib ----------
        Class<?> gameProfileCls = Class.forName("com.mojang.authlib.GameProfile");
        Class<?> propertyCls = Class.forName("com.mojang.authlib.properties.Property");

        Constructor<?> gameProfileCtor = gameProfileCls.getConstructor(java.util.UUID.class, String.class);
        Constructor<?> propertyCtor = propertyCls.getConstructor(String.class, String.class);

        Field propertiesField = gameProfileCls.getDeclaredField("properties");
        propertiesField.setAccessible(true);

        Object propsObj = propertiesField.get(gameProfileCtor.newInstance(
                java.util.UUID.randomUUID(), "dummy"));
        Method putMethod = propsObj.getClass().getMethod("put", Object.class, Object.class);

        // ---------- CraftMetaSkull.profile ----------
        String version;
        {
            String pkg = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
            String[] parts = pkg.split("\\.");
            version = parts.length > 3 ? parts[3] : "";
        }

        Class<?> craftMetaSkullCls;
        try {
            craftMetaSkullCls = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftMetaSkull");
        } catch (ClassNotFoundException e) {
            craftMetaSkullCls = Class.forName("org.bukkit.craftbukkit.inventory.CraftMetaSkull");
        }
        Field profileField = craftMetaSkullCls.getDeclaredField("profile");
        profileField.setAccessible(true);

        // ---------- CraftSkull.setProfile ----------
        Class<?> craftSkullCls;
        try {
            craftSkullCls = Class.forName("org.bukkit.craftbukkit." + version + ".block.CraftSkull");
        } catch (ClassNotFoundException e) {
            craftSkullCls = Class.forName("org.bukkit.craftbukkit.block.CraftSkull");
        }

        // ResolvableProfile support (1.20.5+)
        boolean useResolvable;
        Class<?> resolvableCls = null;
        Constructor<?> resolvableCtor = null;
        try {
            resolvableCls = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
            resolvableCtor = resolvableCls.getConstructor(gameProfileCls);
            useResolvable = true;
        } catch (ClassNotFoundException ignored) {
            useResolvable = false;
        }

        Method setProfileMethod = null;
        try {
            if (useResolvable) {
                try {
                    setProfileMethod = craftSkullCls.getDeclaredMethod("setProfile", resolvableCls);
                } catch (NoSuchMethodException ex) {
                    setProfileMethod = craftSkullCls.getDeclaredMethod("setProfile", gameProfileCls);
                }
            } else {
                setProfileMethod = craftSkullCls.getDeclaredMethod("setProfile", gameProfileCls);
            }
            if (setProfileMethod != null) {
                setProfileMethod.setAccessible(true);
            }
        } catch (NoSuchMethodException ignored) {
            // Block reflection not available on this server; ModernApplier will fallback
        }

        // Fallback: try any method named setProfile or setGameProfile with one parameter
        for (Method m : craftSkullCls.getDeclaredMethods()) {
            if ((m.getName().equals("setProfile") || m.getName().equals("setGameProfile"))
                    && m.getParameterCount() == 1) {
                setProfileMethod = m;
                setProfileMethod.setAccessible(true);
                break;
            }
        }

        // Prefer non-PlayerProfile methods (they write NBT correctly). Iterate twice: first look for non-Bukkit profile param, else fallback.
        if (setProfileMethod == null) {
            Method candidatePlayerProfile = null;
            for (Method m : craftSkullCls.getDeclaredMethods()) {
                if (m.getParameterCount() != 1) continue;
                String nameLower = m.getName().toLowerCase();
                if (!(nameLower.contains("profile"))) continue;
                Class<?> ptype = m.getParameterTypes()[0];
                if (ptype.getName().equals("org.bukkit.profile.PlayerProfile")) {
                    candidatePlayerProfile = m; // consider later
                    continue;
                }
                // choose first non-PlayerProfile param method
                setProfileMethod = m;
                setProfileMethod.setAccessible(true);
                break;
            }
            if (setProfileMethod == null && candidatePlayerProfile != null) {
                setProfileMethod = candidatePlayerProfile;
                setProfileMethod.setAccessible(true);
            }
        }

        // Broader fallback: any method with 1 param where name contains "Profile"
        if (setProfileMethod == null) {
            for (Method m : craftSkullCls.getDeclaredMethods()) {
                if (m.getParameterCount()==1 && m.getName().toLowerCase().contains("profile")) {
                    setProfileMethod = m;
                    setProfileMethod.setAccessible(true);
                    break;
                }
            }
        }

        return new ReflectionContext(
                profileField,
                setProfileMethod,
                gameProfileCls,
                gameProfileCtor,
                propertyCls,
                propertyCtor,
                propertiesField,
                putMethod,
                useResolvable,
                resolvableCls,
                resolvableCtor
        );
    }
}
