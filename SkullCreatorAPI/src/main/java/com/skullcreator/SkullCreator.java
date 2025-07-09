package com.skullcreator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * A library for the Bukkit API to create player skulls
 * from names, base64 strings, and texture URLs.
 * <p>
 * Compatible with Spigot using reflection for GameProfile access.
 * Updated to handle newer Minecraft versions (1.20.5+) that use ResolvableProfile.
 *
 * @author Liparakis on 6/7/2025.
 */
public class SkullCreator {

    // Resolve skull material in a version-safe way at runtime
    private static final Material SKULL_MATERIAL;
    private static final boolean LEGACY_DATA;
    static {
        Material tmp = Material.matchMaterial("PLAYER_HEAD");
        if (tmp == null) tmp = Material.matchMaterial("SKULL_ITEM");
        SKULL_MATERIAL = tmp;
        LEGACY_DATA = "SKULL_ITEM".equals(SKULL_MATERIAL.name());
    }

    // Cached empty skull avoids new ItemStack allocation (durability 3 for legacy)
    private static final ItemStack EMPTY_SKULL = LEGACY_DATA ? new ItemStack(SKULL_MATERIAL, 1, (short) 3)
                                                            : new ItemStack(SKULL_MATERIAL);

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
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(name, "name");

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
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(id, "id");

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        try {
            // 1.13+
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        } catch (NoSuchMethodError ignored) {
            // 1.8–1.12 legacy API
            String name = Bukkit.getOfflinePlayer(id).getName();
            if (name == null) name = id.toString();
            meta.setOwner(name);
        }
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
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(url, "url");

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
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(base64, "base64");

        return com.skullcreator.internal.ProfileResolver.item(item, base64);
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
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(name, "name");

        setToSkull(block);
        Skull state = (Skull) block.getState();
        try {
            state.setOwningPlayer(Bukkit.getOfflinePlayer(name));
        } catch (NoSuchMethodError ignored) {
            state.setOwner(name);
        }
        state.update(false, false);
    }

    /**
     * Sets the block to a skull with the given UUID.
     *
     * @param block The block to set.
     * @param id    The player to set it to.
     */
    public static void blockWithUuid(Block block, UUID id) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(id, "id");

        setToSkull(block);
        Skull state = (Skull) block.getState();
        try {
            state.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        } catch (NoSuchMethodError ignored) {
            String name = Bukkit.getOfflinePlayer(id).getName();
            if (name == null) name = id.toString();
            state.setOwner(name);
        }
        state.update(false, false);
    }

    /**
     * Sets the block to a skull with the skin found at the provided mojang URL.
     *
     * @param block The block to set.
     * @param url   The mojang URL to set it to use.
     */
    public static void blockWithUrl(Block block, String url) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(url, "url");

        blockWithBase64(block, urlToBase64(url));
    }

    /**
     * Sets the block to a skull with the skin for the base64 string.
     *
     * @param block  The block to set.
     * @param base64 The base64 to set it to use.
     */
    public static void blockWithBase64(Block block, String base64) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(base64, "base64");

        setToSkull(block);
        Skull state = (Skull) block.getState();
        // Phase-1: delegate via resolver
        com.skullcreator.internal.ProfileResolver.block(state, base64);
        state.update(true, false);
    }

    private static void setToSkull(Block block) {
        Material blockMat = Material.matchMaterial("PLAYER_HEAD");
        if (blockMat == null) blockMat = Material.matchMaterial("SKULL");
        block.setType(blockMat, false);
    }

    /**
     * Optimized URL to base64 conversion using pre-allocated encoder for better performance.
     */
    private static String urlToBase64(String url) {
        URI actualUrl;
        try {
            actualUrl = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl.toString() + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    /**
     * Clears the profile cache. Useful for memory management in long-running servers.
     */
    public static void clearCache() {
        com.skullcreator.internal.ProfileResolver.clearCache();
    }

    /**
     * Gets the current cache size for monitoring purposes.
     */
    public static int getCacheSize() {
        return com.skullcreator.internal.ProfileResolver.getCacheSize();
    }

    /**
     * Checks if the reflection initialization was successful.
     *
     * @return true if reflection was initialized successfully, false otherwise.
     */
    public static boolean isReflectionInitialized() {
        return com.skullcreator.internal.ProfileResolver.ready();
    }
}