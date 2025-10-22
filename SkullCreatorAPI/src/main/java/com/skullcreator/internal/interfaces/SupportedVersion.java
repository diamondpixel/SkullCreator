package com.skullcreator.internal.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation placed on {@code VersionPatch} implementations to declare the
 * Minecraft version prefix strings they support.
 * <p>
 * Supports two matching modes:
 * <ul>
 *   <li><b>Prefix matching</b>: "1.20" matches any version starting with "1.20"</li>
 *   <li><b>Version and above</b>: "1.21.9+" matches 1.21.9 and all higher versions</li>
 * </ul>
 * <p>
 * Examples:
 * <pre><code>
 * // Prefix matching (multiple specific versions)
 * &#64;SupportedVersion({"1.20", "1.20.1"})
 * public final class Patch120x implements VersionPatch { }
 *
 * // Version and above (all versions from 1.21.9 onwards)
 * &#64;SupportedVersion({"1.21.9+"})
 * public final class Patch1219Plus implements VersionPatch { }
 * </code></pre>
 * The bootstrap loader matches the running server's version (e.g. "1.20.6")
 * against the supplied {@code value()} prefixes using {@code String#startsWith}
 * or numeric version comparison for "+" suffixed entries.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SupportedVersion {
    /**
     * One or more version strings to match.
     * <p>
     * Can be either:
     * <ul>
     *   <li>A prefix like "1.20" (matches any version starting with "1.20")</li>
     *   <li>A minimum version with "+" suffix like "1.21.9+" (matches 1.21.9 and above)</li>
     * </ul>
     */
    String[] value();
}
