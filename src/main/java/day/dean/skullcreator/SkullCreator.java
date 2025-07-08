package day.dean.skullcreator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.UUID;

/**
 * A library for the Bukkit API to create player skulls
 * from names, base64 strings, and texture URLs.
 * <p>
 * Compatible with Spigot using reflection for GameProfile access.
 * Updated to handle newer Minecraft versions (1.20.5+) that use ResolvableProfile.
 *
 * @author Liparakis on 7/7/2025.
 */
public class SkullCreator {

    // Pre-encoded base64 encoder for better performance
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    // Reusable StringBuilder for URL encoding
    private static final ThreadLocal<StringBuilder> URL_BUILDER = ThreadLocal.withInitial(() -> new StringBuilder(128));

    // Pre-created empty skull ItemStack to clone from
    private static final ItemStack EMPTY_SKULL = new ItemStack(Material.PLAYER_HEAD);

    /**
     * Creates a player skull using modern Material.PLAYER_HEAD.
     * Uses cloning for better performance than creating new instances.
     */
    public static ItemStack createSkull() {
        return EMPTY_SKULL.clone();
    }

    /**
     * Creates a player skull item with the skin based on a player's name.
     *
     * @param name The Player's name.
     * @return The head of the Player.
     * @deprecated names don't make for good identifiers.
     */
    @Deprecated
    public static ItemStack itemFromName(String name) {
        return itemWithName(createSkull(), name);
    }

    /**
     * Creates a player skull item with the skin based on a player's UUID.
     *
     * @param id The Player's UUID.
     * @return The head of the Player.
     */
    public static ItemStack itemFromUuid(UUID id) {
        return itemWithUuid(createSkull(), id);
    }

    /**
     * Creates a player skull item with the skin at a Mojang URL.
     *
     * @param url The Mojang URL.
     * @return The head of the Player.
     */
    public static ItemStack itemFromUrl(String url) {
        return itemWithUrl(createSkull(), url);
    }

    /**
     * Creates a player skull item with the skin based on a base64 string.
     *
     * @param base64 The Base64 string.
     * @return The head of the Player.
     */
    public static ItemStack itemFromBase64(String base64) {
        return itemWithBase64(createSkull(), base64);
    }

    /**
     * Modifies a skull to use the skin of the player with a given name.
     *
     * @param item The item to apply the name to. Must be a player skull.
     * @param name The Player's name.
     * @return The head of the Player.
     * @deprecated names don't make for good identifiers.
     */
    @Deprecated
    public static ItemStack itemWithName(ItemStack item, String name) {
        validateNotNull(item, "item");
        validateNotNull(name, "name");

        UUID id = Bukkit.getOfflinePlayer(name).getUniqueId();
        return itemWithUuid(item, id);
    }

    /**
     * Modifies a skull to use the skin of the player with a given UUID.
     *
     * @param item The item to apply the name to. Must be a player skull.
     * @param id   The Player's UUID.
     * @return The head of the Player.
     */
    public static ItemStack itemWithUuid(ItemStack item, UUID id) {
        validateNotNull(item, "item");
        validateNotNull(id, "id");

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Modifies a skull to use the skin at the given Mojang URL.
     *
     * @param item The item to apply the skin to. Must be a player skull.
     * @param url  The URL of the Mojang skin.
     * @return The head associated with the URL.
     */
    public static ItemStack itemWithUrl(ItemStack item, String url) {
        validateNotNull(item, "item");
        validateNotNull(url, "url");

        return itemWithBase64(item, urlToBase64(url));
    }

    /**
     * Modifies a skull to use the skin based on the given base64 string.
     *
     * @param item   The ItemStack to put the base64 onto. Must be a player skull.
     * @param base64 The base64 string containing the texture.
     * @return The head with a custom texture.
     */
    public static ItemStack itemWithBase64(ItemStack item, String base64) {
        validateNotNull(item, "item");
        validateNotNull(base64, "base64");

        // Phase-1: delegate via resolver to keep call-site stable
        return day.dean.skullcreator.internal.ProfileResolver.item(item, base64);
    }

    /**
     * Sets the block to a skull with the given name.
     *
     * @param block The block to set.
     * @param name  The player to set it to.
     * @deprecated names don't make for good identifiers.
     */
    @Deprecated
    public static void blockWithName(Block block, String name) {
        validateNotNull(block, "block");
        validateNotNull(name, "name");

        setToSkull(block);
        Skull state = (Skull) block.getState();
        state.setOwningPlayer(Bukkit.getOfflinePlayer(name));
        state.update(false, false);
    }

    /**
     * Sets the block to a skull with the given UUID.
     *
     * @param block The block to set.
     * @param id    The player to set it to.
     */
    public static void blockWithUuid(Block block, UUID id) {
        validateNotNull(block, "block");
        validateNotNull(id, "id");

        setToSkull(block);
        Skull state = (Skull) block.getState();
        state.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        state.update(false, false);
    }

    /**
     * Sets the block to a skull with the skin found at the provided mojang URL.
     *
     * @param block The block to set.
     * @param url   The mojang URL to set it to use.
     */
    public static void blockWithUrl(Block block, String url) {
        validateNotNull(block, "block");
        validateNotNull(url, "url");

        blockWithBase64(block, urlToBase64(url));
    }

    /**
     * Sets the block to a skull with the skin for the base64 string.
     *
     * @param block  The block to set.
     * @param base64 The base64 to set it to use.
     */
    public static void blockWithBase64(Block block, String base64) {
        validateNotNull(block, "block");
        validateNotNull(base64, "base64");

        setToSkull(block);
        Skull state = (Skull) block.getState();
        // Phase-1: delegate via resolver
        day.dean.skullcreator.internal.ProfileResolver.block(state, base64);
        state.update(true, false);
    }

    private static void setToSkull(Block block) {
        block.setType(Material.PLAYER_HEAD, false);
    }

    /**
     * Optimized validation method with inline expansion.
     */
    private static void validateNotNull(Object obj, String name) {
        if (obj == null) {
            throw new NullPointerException(name + " should not be null!");
        }
    }

    /**
     * Optimized URL to base64 conversion using ThreadLocal StringBuilder
     * and pre-allocated encoder for better performance.
     */
    private static String urlToBase64(String url) {
        URI actualUrl;
        try {
            actualUrl = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // Use ThreadLocal StringBuilder to avoid object creation
        StringBuilder builder = URL_BUILDER.get();
        builder.setLength(0); // Clear previous content

        builder.append("{\"textures\":{\"SKIN\":{\"url\":\"")
                .append(actualUrl.toString())
                .append("\"}}}");

        return BASE64_ENCODER.encodeToString(builder.toString().getBytes());
    }

    /**
     * Clears the profile cache. Useful for memory management in long-running servers.
     */
    public static void clearCache() {
        day.dean.skullcreator.internal.ProfileResolver.clearCache();
    }

    /**
     * Gets the current cache size for monitoring purposes.
     */
    public static int getCacheSize() {
        return day.dean.skullcreator.internal.ProfileResolver.getCacheSize();
    }

    /**
     * Checks if the reflection initialization was successful.
     *
     * @return true if reflection was initialized successfully, false otherwise.
     */
    public static boolean isReflectionInitialized() {
        return day.dean.skullcreator.internal.ProfileResolver.ready();
    }
}