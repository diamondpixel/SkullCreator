package com.skullcreator.internal.strategy;

import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Skull;
import com.skullcreator.internal.bootstrap.ReflectionContext;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;

/**
 * Fastest {@link ProfileApplier} implementation: it reflects directly into CraftBukkit and
 * Mojang-Authlib classes resolved during {@link com.skullcreator.internal.bootstrap.ReflectionBootstrap}.
 * <p>
 * If the necessary classes, fields, or methods cannot be resolved at startup the bootstrapper
 * falls back to {@link FallbackApplier}, so this class is only instantiated when reflection
 * is available. No additional runtime checks are required here.
 */
public class ModernApplier implements ProfileApplier {

    private final ReflectionContext ctx;

    public ModernApplier(ReflectionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public ItemStack applyToItem(ItemStack item, String base64) {
        if (!(item.getItemMeta() instanceof SkullMeta)) return item;
        try {
            SkullMeta meta = (SkullMeta) item.getItemMeta();

            Object gameProfile = getCachedGameProfile(base64);
            if (gameProfile == null) return item;

            Object targetProfile = gameProfile;
            if (ctx.useResolvableProfile && ctx.resolvableProfileClass != null && ctx.resolvableProfileCtor != null 
                    && ctx.profileField.getType().isAssignableFrom(ctx.resolvableProfileClass)) {
                targetProfile = ctx.resolvableProfileCtor.newInstance(gameProfile);
            }

            // First try direct field injection if compatible
            if (ctx.profileField.getType().isAssignableFrom(targetProfile.getClass())) {
                ctx.profileField.set(meta, targetProfile);
            } else {
                // Fallback to public/protected setter methods via reflection
                boolean applied = false;
                try {
                    java.lang.reflect.Method ownerM = meta.getClass().getMethod("setOwnerProfile", targetProfile.getClass());
                    ownerM.invoke(meta, targetProfile);
                    applied = true;
                } catch (NoSuchMethodException ignored) {}
                if (!applied) {
                    try {
                        java.lang.reflect.Method m = meta.getClass().getMethod("setPlayerProfile", targetProfile.getClass());
                        m.invoke(meta, targetProfile);
                        applied = true;
                    } catch (NoSuchMethodException ignored) {}
                }
                if (!applied) {
                    // Last resort: if field type is GameProfile and we have wrapper, unwrap
                    if (targetProfile.getClass().getSimpleName().equals("ResolvableProfile")) {
                        java.lang.reflect.Method gpGetter = targetProfile.getClass().getMethod("asProfile");
                        Object raw = gpGetter.invoke(targetProfile);
                        if (ctx.profileField.getType().isAssignableFrom(raw.getClass())) {
                            ctx.profileField.set(meta, raw);
                        }
                    }
                }
            }
            item.setItemMeta(meta);
        } catch (Exception e) {
            // Fallback silently to avoid scary logs when alternative path works
            return new FallbackApplier().applyToItem(item, base64);
        }
        return item;
    }

    @Override
    public void applyToBlock(Skull skull, String base64) {
        // Cleaned: production path without debug spam
        // Try preferred Bukkit API path first (1.19+)
        try {
            java.lang.reflect.Method create = Bukkit.class.getMethod("createProfile", UUID.class, String.class);
            Object playerProfile = create.invoke(null, UUID.randomUUID(), null);

            // inject texture via helper; ensure fallback to direct setProperty if helper failed
            com.skullcreator.internal.util.TexturePropertyWriter.injectTexture(playerProfile, base64);
            // verify property exists; if not, try PlayerProfile#setProperty(String,String)
            boolean hasTexture = false;
            for (java.lang.reflect.Method gm : playerProfile.getClass().getMethods()) {
                if (gm.getName().equals("hasProperty") && gm.getParameterCount()==1) {
                    hasTexture = (boolean) gm.invoke(playerProfile, "textures");
                    break;
                }
            }
            if (!hasTexture) {
                try {
                    java.lang.reflect.Method setProp = playerProfile.getClass().getMethod("setProperty", String.class, String.class);
                    setProp.invoke(playerProfile, "textures", base64);
                } catch (Exception ignored) {}
            }

            java.lang.reflect.Method setter = null;
            for (java.lang.reflect.Method m : skull.getClass().getMethods()) {
                if (m.getName().equals("setPlayerProfile") && m.getParameterCount() == 1) { setter = m; break; }
                if (m.getName().equals("setOwnerProfile") && m.getParameterCount() == 1) { setter = m; }
            }
            if (setter != null) {
                setter.invoke(skull, playerProfile);
                skull.update(true);
                return; // success
            }
        } catch (Exception ignored) {}

        // Reflection path
        if (ctx.setProfileMethod == null) {
            new FallbackApplier().applyToBlock(skull, base64);
            return;
        }
        try {
            Object gameProfile = getCachedGameProfile(base64);
            if (gameProfile == null) return;

            Object target;
            Class<?> param = ctx.setProfileMethod.getParameterTypes()[0];
            if (ctx.useResolvableProfile && ctx.resolvableProfileClass != null && param.isAssignableFrom(ctx.resolvableProfileClass)) {
                target = ctx.resolvableProfileCtor.newInstance(gameProfile);
            } else if (param.getName().equals("org.bukkit.profile.PlayerProfile")) {
                java.util.UUID id = java.util.UUID.nameUUIDFromBytes(("SkullCreator:" + base64).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                java.lang.reflect.Method createPP;
                try {
                    createPP = Bukkit.class.getMethod("createPlayerProfile", java.util.UUID.class, String.class);
                } catch (NoSuchMethodException ne) {
                    createPP = Bukkit.class.getMethod("createProfile", java.util.UUID.class, String.class);
                }
                Object playerProfile = createPP.invoke(null, id, null);
                com.skullcreator.internal.util.TexturePropertyWriter.injectTexture(playerProfile, base64);
                target = playerProfile;
            } else {
                target = gameProfile;
            }

            ctx.setProfileMethod.invoke(skull, target);
            skull.update(true);
        } catch (Exception e) {
            // swallow and fallback silently
            new FallbackApplier().applyToBlock(skull, base64);
        }
    }

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    public void clearCache() {
        cache.clear();
    }

    public int getCacheSize() {
        return cache.size();
    }

    private Object getCachedGameProfile(String base64) {
        return cache.computeIfAbsent(base64, this::createGameProfile);
    }

    private Object createGameProfile(String base64) {
        try {
            UUID uuid = deterministicUUID(base64);
            Object profile = ctx.gameProfileCtor.newInstance(uuid, "");

            Object props = ctx.propertiesField.get(profile);
            Object texProp = ctx.propertyCtor.newInstance("textures", base64);
            ctx.putMethod.invoke(props, "textures", texProp);

            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    private UUID deterministicUUID(String base64) {
        long hash = base64.hashCode();
        return new UUID(hash, hash);
    }
}
