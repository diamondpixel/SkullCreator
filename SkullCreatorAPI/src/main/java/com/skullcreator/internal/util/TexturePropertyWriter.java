package com.skullcreator.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility helper to inject a base64 textures property into any supported
 * Bukkit / Mojang-authlib PlayerProfile implementation via reflection.
 *
 * Isolated in its own class to keep reflection-heavy code away from
 * the core logic and to enable unit-testing without a server runtime.
 */
public final class TexturePropertyWriter {
    private static final Logger LOGGER = Logger.getLogger(TexturePropertyWriter.class.getName());
    private TexturePropertyWriter() {}

    public static void injectTexture(Object profileObj, String base64) {
        if (profileObj == null || base64 == null) return;

        // 1) Modern PlayerTextures API (1.19.3+)
        try {
            Method getTex = profileObj.getClass().getMethod("getTextures");
            Object textures = getTex.invoke(profileObj);
            if (textures != null) {
                // Extract URL from Base64 JSON if present
                URL url = extractUrl(base64);
                if (url != null) {
                    Method setSkin;
                    try {
                        setSkin = textures.getClass().getMethod("setSkin", URL.class);
                    } catch (NoSuchMethodException nf) {
                        // method with model param
                        setSkin = textures.getClass().getMethod("setSkin", URL.class, Class.forName("org.bukkit.profile.PlayerTextures$SkinModel"));
                    }
                    setSkin.invoke(textures, url);
                    // Ensure changes are stored back in profile using PlayerTextures interface
                    Class<?> ptIface = Class.forName("org.bukkit.profile.PlayerTextures");
                    Method setTextures = profileObj.getClass().getMethod("setTextures", ptIface);
                    setTextures.invoke(profileObj, textures);
                    // fall through to also inject raw property for backward compatibility
                    if (System.getProperty("DEBUG") != null) {
                        LOGGER.log(Level.FINE, "Falling through to inject raw property for backward compatibility");
                    }
                }
            }
        } catch (Exception ignored) {}

        // 2) Legacy Spigot API: setProperty(String,String)
        for (Method m : profileObj.getClass().getMethods()) {
            if (m.getName().equals("setProperty")) {
                Class<?>[] pt = m.getParameterTypes();
                if (pt.length == 2 && pt[0] == String.class && pt[1] == String.class) {
                    try { m.invoke(profileObj, "textures", base64); return; } catch (Exception ignored) {}
                }
            }
        }
        // 3) Deep reflection into Authlib PropertyMap
        try {
            Method getProps = profileObj.getClass().getMethod("getProperties");
            Object props = getProps.invoke(profileObj);
            Class<?> propClass = Class.forName("com.mojang.authlib.properties.Property");
            Constructor<?> propCtor;
            Object propObj;
            try {
                propCtor = propClass.getConstructor(String.class, String.class);
                propObj = propCtor.newInstance("textures", base64);
            } catch (NoSuchMethodException nf) {
                // Older authlib (1.8) uses (String, String, String)
                propCtor = propClass.getConstructor(String.class, String.class, String.class);
                propObj = propCtor.newInstance("textures", base64, "");
            }

            try {
                Method put = props.getClass().getMethod("put", Object.class, Object.class);
                put.invoke(props, "textures", propObj);
            } catch (NoSuchMethodException ex) {
                Method add = props.getClass().getDeclaredMethod("add", Object.class);
                add.setAccessible(true);
                add.invoke(props, propObj);
            }
        } catch (Exception ignored) {}
    }

    // naive extractor
    private static URL extractUrl(String b64) {
        try {
            String json = new String(Base64.getDecoder().decode(b64));
            int idx = json.indexOf("\"url\":\"");
            if (idx == -1) return null;
            int start = idx + 7;
            int end = json.indexOf('"', start);
            if (end == -1) return null;
            String urlStr = json.substring(start, end).replace("\\/", "/");
            return new URL(urlStr);
        } catch (Exception e) {
            return null;
        }
    }
}
