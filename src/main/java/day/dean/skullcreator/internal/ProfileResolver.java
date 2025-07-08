package day.dean.skullcreator.internal;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.block.Skull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import day.dean.skullcreator.internal.strategy.ProfileApplier;
import day.dean.skullcreator.internal.strategy.FallbackApplier;
import day.dean.skullcreator.internal.strategy.ModernApplier;
import day.dean.skullcreator.internal.bootstrap.ReflectionBootstrap;

/**
 * Handles reflection-based profile creation and application logic, abstracted away
 * from the public {@code SkullCreator} API. All heavy compatibility logic lives here.
 */
public final class ProfileResolver {

    /* ---------------- Shared cache & constants ---------------- */

    private static final ConcurrentHashMap<String, Object> PROFILE_CACHE = new ConcurrentHashMap<>();

    // Reflection fields / methods
    private static Field profileField;
    private static Method setProfileMethod;
    private static Class<?> gameProfileClass;
    private static Class<?> propertyClass;
    private static Constructor<?> propertyConstructor;
    private static Constructor<?> gameProfileConstructor;
    private static Method putMethod;
    private static Field propertiesField;

    // ResolvableProfile support (1.20.5+)
    private static Class<?> resolvableProfileClass;
    private static Constructor<?> resolvableProfileConstructor;
    private static boolean useResolvableProfile = false;

    // Initialisation status
    private static boolean reflectionInitialized = false;
    private static String initializationError = "Unknown error";

    private ProfileResolver() {
        /* no instance */
    }

    /* ---------------- Public facade ---------------- */

    public static boolean ready() {
        return reflectionInitialized;
    }

    /* ---------------- Strategy delegation ---------------- */
    private static final ProfileApplier APPLIER;

    static {
        APPLIER = ReflectionBootstrap.bootstrap();
    }

    public static ItemStack item(ItemStack item, String base64) {
        return APPLIER.applyToItem(item, base64);
    }

    public static void block(Skull skullState, String base64) {
        APPLIER.applyToBlock(skullState, base64);
    }

    /* ---------------- Reflection initialisation ---------------- */

    public static void initializeReflection() {
        try {
            String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
            String[] parts = serverPackage.split("\\.");

            String serverVersion = parts.length > 3 ? parts[3] : "";

            boolean success = false;
            Exception lastEx = null;

            if (!serverVersion.isEmpty()) {
                try {
                    success = initializeModernReflection(serverVersion);
                } catch (Exception e) { lastEx = e; }
            }
            if (!success && !serverVersion.isEmpty()) {
                try {
                    success = initializeLegacyReflection(serverVersion);
                } catch (Exception e) { lastEx = e; }
            }
            if (!success) {
                try {
                    success = initializeVersionlessReflection();
                } catch (Exception e) { lastEx = e; }
            }

            reflectionInitialized = success;
            if (!success) {
                initializationError = lastEx != null ? lastEx.getClass().getSimpleName() + ": " + lastEx.getMessage() : "Unknown";
            }
        } catch (Exception e) {
            reflectionInitialized = false;
            initializationError = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private static boolean initializeModernReflection(String version) throws Exception {
        gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        propertyClass = Class.forName("com.mojang.authlib.properties.Property");

        gameProfileConstructor = gameProfileClass.getConstructor(UUID.class, String.class);
        propertyConstructor = propertyClass.getConstructor(String.class, String.class);

        propertiesField = gameProfileClass.getDeclaredField("properties");
        propertiesField.setAccessible(true);

        Object testProfile = gameProfileConstructor.newInstance(UUID.randomUUID(), "dummy");
        Object props = propertiesField.get(testProfile);
        putMethod = props.getClass().getMethod("put", Object.class, Object.class);

        // CraftMetaSkull profile field – try versioned then unversioned package
        Class<?> craftMetaSkullClass;
        try {
            craftMetaSkullClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftMetaSkull");
        } catch (ClassNotFoundException e) {
            craftMetaSkullClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftMetaSkull");
        }
        profileField = craftMetaSkullClass.getDeclaredField("profile");
        profileField.setAccessible(true);

        // CraftSkull setProfile method
        try {
            resolvableProfileClass = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
            useResolvableProfile = true;
            resolvableProfileConstructor = resolvableProfileClass.getConstructor(gameProfileClass);
        } catch (ClassNotFoundException ignored) {
            useResolvableProfile = false;
        }

        try {
            Class<?> craftSkullClass;
            try {
                craftSkullClass = Class.forName("org.bukkit.craftbukkit." + version + ".block.CraftSkull");
            } catch (ClassNotFoundException e) {
                craftSkullClass = Class.forName("org.bukkit.craftbukkit.block.CraftSkull");
            }
            if (useResolvableProfile) {
                try {
                    setProfileMethod = craftSkullClass.getDeclaredMethod("setProfile", resolvableProfileClass);
                } catch (NoSuchMethodException e) {
                    setProfileMethod = craftSkullClass.getDeclaredMethod("setProfile", gameProfileClass);
                }
            } else {
                setProfileMethod = craftSkullClass.getDeclaredMethod("setProfile", gameProfileClass);
            }
            setProfileMethod.setAccessible(true);
        } catch (Exception ignored) {
            // optional for items.
        }

        return true;
    }

    private static boolean initializeLegacyReflection(String version) throws Exception {
        // Very similar to modern but field names differ
        return initializeModernReflection(version); // works for most 1.8–1.12 builds
    }

    private static boolean initializeVersionlessReflection() throws Exception {
        return initializeModernReflection("");
    }

    /* ---------------- Profile helpers ---------------- */

    public static ItemStack applyProfileToItemInternal(ItemStack item, String base64) {
        if (!(item.getItemMeta() instanceof SkullMeta)) {
            return item;
        }
        try {
            SkullMeta meta = (SkullMeta) item.getItemMeta();

            Object gameProfile = getCachedGameProfile(base64);
            if (gameProfile == null) return item;

            Object profileObj = gameProfile;
            if (useResolvableProfile) {
                profileObj = resolvableProfileConstructor.newInstance(gameProfile);
            }
            profileField.set(meta, profileObj);
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            Bukkit.getLogger().warning("SkullCreator: Failed to apply profile to item: " + e.getMessage());
            return item;
        }
    }

    private static ItemStack applyProfileToItemFallback(ItemStack item, String base64) {
        try {
            if (!(item.getItemMeta() instanceof SkullMeta)) {
                return item;
            }

            // create empty profile and inject texture
            org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            injectTexture(profile, base64);

            SkullMeta meta = (SkullMeta) item.getItemMeta();
            try {
                meta.setOwnerProfile(profile); // Spigot/Paper 1.19+
            } catch (NoSuchMethodError ignored) {
                try {
                    // Older Paper naming
                    Method setPlayerProfile = meta.getClass().getMethod("setPlayerProfile", profile.getClass());
                    setPlayerProfile.invoke(meta, profile);
                } catch (Exception e) {
                    return item; // can't set
                }
            }

            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            Bukkit.getLogger().warning("SkullCreator: Fallback item profile failed");
            return item;
        }
    }

    public static void applyProfileToBlockInternal(Skull skull, String base64) {
        try {
            Object gameProfile = getCachedGameProfile(base64);
            if (gameProfile == null) return;

            Object target = gameProfile;
            if (useResolvableProfile && setProfileMethod.getParameterTypes()[0].isAssignableFrom(resolvableProfileClass)) {
                target = resolvableProfileConstructor.newInstance(gameProfile);
            }
            setProfileMethod.invoke(skull, target);
        } catch (Exception e) {
            Bukkit.getLogger().warning("SkullCreator: Failed to apply profile to block");
        }
    }

    private static void applyProfileToBlockFallback(Skull skull, String base64) {
        try {
            // create empty profile compatible with CraftSkull and inject texture
            Method createProfile = Bukkit.class.getMethod("createProfile", UUID.class, String.class);
            Object playerProfile = createProfile.invoke(null, UUID.randomUUID(), null);
            injectTexture(playerProfile, base64);

            // apply profile to skull
            Method setPlayerProfile = null;
            for (Method m : skull.getClass().getMethods()) {
                if (m.getName().equals("setPlayerProfile")) {
                    setPlayerProfile = m;
                    break;
                }
            }
            if (setPlayerProfile != null) {
                setPlayerProfile.invoke(skull, playerProfile);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("SkullCreator: Fallback profile application failed");
        }
    }

    private static Object getCachedGameProfile(String base64) {
        return PROFILE_CACHE.computeIfAbsent(base64, b -> createGameProfile(b));
    }

    private static Object createGameProfile(String base64) {
        try {
            UUID uuid = generateDeterministicUUID(base64);
            Object profile = gameProfileConstructor.newInstance(uuid, "");

            Object props = propertiesField.get(profile);
            Object property = propertyConstructor.newInstance("textures", base64);
            putMethod.invoke(props, "textures", property);

            return profile;
        } catch (Exception e) {
            Bukkit.getLogger().severe("SkullCreator: Failed to create GameProfile: " + e.getMessage());
            return null;
        }
    }

    private static UUID generateDeterministicUUID(String base64) {
        long hash = base64.hashCode();
        return new UUID(hash, hash);
    }

    /* ---------------- Utility ---------------- */

    /**
     * Injects a base64 textures property into any supported PlayerProfile implementation.
     * Handles Spigot, Paper, and raw Mojang Authlib objects via reflection.
     */
    private static void injectTexture(Object profileObj, String base64) {
        if (profileObj == null || base64 == null) return;

        // 1) Spigot API: setProperty(String,String)
        for (Method m : profileObj.getClass().getMethods()) {
            if (m.getName().equals("setProperty")) {
                Class<?>[] pt = m.getParameterTypes();
                if (pt.length == 2 && pt[0] == String.class && pt[1] == String.class) {
                    try { m.invoke(profileObj, "textures", base64); return; } catch (Exception ignored) {}
                }
            }
        }

        // 2) Paper ProfileProperty route
        try {
            Class<?> propCls = Class.forName("com.destroystokyo.paper.profile.ProfileProperty");
            Method setProp = profileObj.getClass().getMethod("setProperty", propCls);
            Constructor<?> ctor = propCls.getConstructor(String.class, String.class);
            Object propObj = ctor.newInstance("textures", base64);
            setProp.invoke(profileObj, propObj);
            return;
        } catch (Exception ignored) {}

        // 3) Deep reflection into Authlib PropertyMap
        try {
            Method getProps = profileObj.getClass().getMethod("getProperties");
            Object props = getProps.invoke(profileObj);
            Class<?> propClass = Class.forName("com.mojang.authlib.properties.Property");
            Constructor<?> propCtor = propClass.getConstructor(String.class, String.class);
            Object texProp = propCtor.newInstance("textures", base64);

            try {
                Method put = props.getClass().getMethod("put", Object.class, Object.class);
                put.invoke(props, "textures", texProp);
            } catch (NoSuchMethodException ex) {
                Method add = props.getClass().getDeclaredMethod("add", Object.class);
                add.setAccessible(true);
                add.invoke(props, texProp);
            }
        } catch (Exception ignored) {}
    }

    /* ---------------- Cache helpers exposed ---------------- */

    public static void clearCache() {
        PROFILE_CACHE.clear();
    }

    public static int getCacheSize() {
        return PROFILE_CACHE.size();
    }

    public static boolean hasBlockReflection() {
        return setProfileMethod != null;
    }
}
