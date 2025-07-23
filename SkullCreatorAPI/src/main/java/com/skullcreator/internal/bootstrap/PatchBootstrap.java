package com.skullcreator.internal.bootstrap;

import com.skullcreator.internal.PatchesRegistry;
import com.skullcreator.internal.interfaces.VersionPatch;
import org.bukkit.Bukkit;

/**
 * Bootstrap helper that locates and applies the correct {@link VersionPatch}
 * for the running Minecraft server version.  This is a lightweight substitute
 * for {@code ServiceLoader}: patches self-register via {@link PatchesRegistry}.
 */
public final class PatchBootstrap {
    private PatchBootstrap() {}

    /**
     * Discovers the first registered patch that declares support for the
     * current server version and exposes it via {@link #active()} so callers can
     * delegate texture application to the selected {@link VersionPatch}.
     *
     * @return {@code true} if a matching patch was found and applied, otherwise {@code false}
     */
    public static boolean bootstrap() {
        String version = Bukkit.getBukkitVersion();

        // Ensure patch classes are loaded so their static blocks register them
        try {
            String[] patchClasses = {
                    "com.skullcreator.internal.patches.v18x_120x",
                    "com.skullcreator.internal.patches.v121x"
            };
            ClassLoader cl = PatchBootstrap.class.getClassLoader();
            for (String cn : patchClasses) {
                try {
                    Class.forName(cn, true, cl);
                } catch (ClassNotFoundException ignored) {}
            }
        } catch (Throwable ignored) {}

        VersionPatch patch = PatchesRegistry.select(version);
        if (patch != null) {
            ACTIVE = patch;
            Bukkit.getLogger().info("[SkullCreator] Selected patch " + patch.getClass().getSimpleName());
            return true;
        } else {
            Bukkit.getLogger().warning("[SkullCreator] No patch available for " + version);
        }
        return false;
    }

    private static volatile VersionPatch ACTIVE;

    /** Returns the patch selected during {@link #bootstrap()}, or {@code null} if none. */
    public static VersionPatch active() {
        return ACTIVE;
    }
}
