package com.skullcreator.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Utility helper to inject a base64 textures property into any supported
 * Bukkit / Paper / Mojang-authlib PlayerProfile implementation via reflection.
 *
 * Isolated in its own class to keep reflection-heavy code away from
 * the core logic and to enable unit-testing without a server runtime.
 */
public final class TexturePropertyWriter {
    private TexturePropertyWriter() {}

    public static void injectTexture(Object profileObj, String base64) {
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
}
