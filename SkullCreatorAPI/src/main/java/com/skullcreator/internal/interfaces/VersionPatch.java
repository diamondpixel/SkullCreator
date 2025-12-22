package com.skullcreator.internal.interfaces;

/**
 * Contract for Minecraft version-specific patches that know how to embed a custom<br>
 * Base64 skin texture into player-head <i>items</i> and <i>block states</i> for a
 * particular range of server builds.
 * <p>
 * Usage overview:
 * <ol>
 *   <li>Each concrete patch advertises its supported version prefixes via the
 *       {@link SupportedVersion} annotation <em>or</em> by overriding
 *       {@link #supports(String)} for more complex logic.</li>
 *   <li>Patches register themselves at class-load time using
 *       {@code PatchesRegistry.register(this)} inside a static block.</li>
 *   <li>{@link com.skullcreator.internal.bootstrap.PatchBootstrap#bootstrap()}
 *       picks the first registered patch whose {@code supports(..)} method
 *       returns {@code true} for the running server, then exposes it through
 *       {@link com.skullcreator.internal.bootstrap.PatchBootstrap#active()}.</li>
 *   <li>Public API callers go through {@link com.skullcreator.internal.TextureApplier}
 *       which delegates to the active patch.</li>
 * </ol>
 * This interface therefore contains only the two operations actually needed at
 * runtime: mutate an {@link org.bukkit.inventory.ItemStack} or a placed
 * {@link org.bukkit.block.Skull}. Patch classes should keep internal state
 * (e.g. reflection caches) private and may optionally expose {@link #clearCache}
 * and {@link #getCacheSize} for monitoring.
 */
public interface VersionPatch {

    /**
     * Whether this patch supports the supplied Minecraft version string.
     * Implementations can rely on the {@link SupportedVersion} annotation instead
     * of overriding this method. If the annotation is present, the default
     * implementation performs a prefix check on all declared values.
     * <p>
     * Supports the "+" suffix to indicate "this version and above".
     * Example: "1.21.9+" will match 1.21.9, 1.21.10, 1.21.11, etc.
     *
     * @param version full Bukkit version, e.g. "1.20.6-R0.1-SNAPSHOT"
     * @return {@code true} if the patch should be used
     */
    default boolean supports(String version) {
        SupportedVersion ann = this.getClass().getAnnotation(SupportedVersion.class);
        if (ann == null) {
            return false;
        }
        for (String prefix : ann.value()) {
            // Check for "+" suffix indicating "this version and above"
            if (prefix.endsWith("+")) {
                String minVersion = prefix.substring(0, prefix.length() - 1);
                if (compareVersions(version, minVersion) >= 0) {
                    return true;
                }
            } else if (version.startsWith(prefix)) {
                // Ensure we don't match partial version numbers
                // e.g., "1.21" should match "1.21-R0.1" but not "1.21.10"
                int nextIdx = prefix.length();
                if (nextIdx >= version.length()) {
                    return true; // Exact match
                }
                char nextChar = version.charAt(nextIdx);
                // Next char should be '-', '_', or any non-digit separator (not '.' followed by digit)
                if (nextChar != '.' && !Character.isDigit(nextChar)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares two version strings numerically.
     * Only compares the numeric parts (e.g., "1.21.9" from "1.21.9-R0.1-SNAPSHOT").
     *
     * @param version1 first version string
     * @param version2 second version string
     * @return negative if version1 &lt; version2, zero if equal, positive if version1 &gt; version2
     */
    default int compareVersions(String version1, String version2) {
        // Extract numeric version parts (e.g., "1.21.9" from "1.21.9-R0.1-SNAPSHOT")
        String v1 = extractNumericVersion(version1);
        String v2 = extractNumericVersion(version2);

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }

    /**
     * Extracts the numeric version portion from a full version string.
     * Example: "1.21.9-R0.1-SNAPSHOT" → "1.21.9"
     */
    default String extractNumericVersion(String version) {
        // Find the first non-numeric separator like '-' or '_'
        int endIndex = version.length();
        for (int i = 0; i < version.length(); i++) {
            char c = version.charAt(i);
            if (c == '-' || c == '_') {
                endIndex = i;
                break;
            }
        }
        return version.substring(0, endIndex);
    }

    /**
     * Parses a version part string to an integer, handling non-numeric parts gracefully.
     */
    default int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Apply the texture to a skull ItemStack.
     * @param item   player-head ItemStack (SkullMeta)
     * @param base64 Base64-encoded texture string
     * @return mutated ItemStack (can be the same instance for chaining)
     */
    org.bukkit.inventory.ItemStack applyToItem(org.bukkit.inventory.ItemStack item, String base64);

    /**
     * Apply the texture to a placed Skull block state.
     * @param skull  Skull block state
     * @param base64 Base64-encoded texture string
     */
    void applyToBlock(org.bukkit.block.Skull skull, String base64);

    /** Clears any internal caches kept by the patch (optional). */
    default void clearCache() {}

    /** Returns the size of any internal caches (optional, default 0). */
    default int getCacheSize() { return 0; }
}
