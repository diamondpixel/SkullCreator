package com.skullcreator.demo;

import com.skullcreator.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Comprehensive test plugin for SkullCreator library.
 * Tests all available functions and methods.
 *
 * @author Liparakis on 8/7/2025.
 */
public class SkullCreatorTesterPlugin extends JavaPlugin {

    private static final String TEST_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDllY2NjNWMxYzc5YWE3ODI2YTE1YTdmNWYxMmZiNDAzMjgxNTdjNTI0MjE2NGJhMmFlZjQ3ZTVkZTlhNWNmYyJ9fX0=";
    private static final String TEST_URL = "http://textures.minecraft.net/texture/955d611a878e821231749b2965708cad942650672db09e26847a88e2fac2946";
    private static final String TEST_PLAYER_NAME = "diamondpixel";
    private static final UUID TEST_PLAYER_UUID = UUID.fromString("7cdf75c8-68dd-4aca-8706-d8137ad03d02"); // diamondpixel's UUID


    public void onEnable() {
        if (!getDescription().getVersion().endsWith("SNAPSHOT")) {
            throw new IllegalStateException("This is not intended to run as a plugin!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String lbl, String[] args) {
        boolean isPlayer = sender instanceof Player;
        Player p = isPlayer ? (Player) sender : null;

        if (args.length == 0) {
            if (isPlayer) {
                showHelp(p);
            } else {
                sender.sendMessage("Usage: /skulltest comprehensive | clear | info");
            }
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "item":
                    testItemMethods(p, args);
                    break;
                case "block":
                    testBlockMethods(p, args);
                    break;
                case "utility":
                    testUtilityMethods(p);
                    break;
                case "comprehensive": {
                    if (isPlayer) runComprehensiveTest(p);
                    else runComprehensiveHeadless(sender);
                    break;
                }

                case "clear":
                    SkullCreator.clearCache();
                    send(p, ChatColor.GREEN + "Cache cleared! New size: " + SkullCreator.getCacheSize());
                    break;
                case "info":
                    showInfo(p);
                    break;
                default:
                    showHelp(p);
                    break;
            }
        } catch (Exception e) {
            if (isPlayer) send(p, ChatColor.RED + "Error: " + e.getMessage());
            else sender.sendMessage("Error: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private void testItemMethods(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§eItem test options:");
            p.sendMessage("§7- /skulltest item base64 - Test base64 skull");
            p.sendMessage("§7- /skulltest item url - Test URL skull");
            p.sendMessage("§7- /skulltest item name - Test name skull (deprecated)");
            p.sendMessage("§7- /skulltest item uuid - Test UUID skull");
            p.sendMessage("§7- /skulltest item all - Test all item methods");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "base64":
                ItemStack base64Skull = SkullCreator.itemFromBase64(TEST_SKULL);
                p.getInventory().addItem(base64Skull);
                p.sendMessage("§aGiven skull from base64!");
                break;

            case "url":
                ItemStack urlSkull = SkullCreator.itemFromUrl(TEST_URL);
                p.getInventory().addItem(urlSkull);
                p.sendMessage("§aGiven skull from URL!");
                break;

            case "name":
                ItemStack nameSkull = SkullCreator.itemFromName(TEST_PLAYER_NAME);
                p.getInventory().addItem(nameSkull);
                p.sendMessage("§aGiven skull from name (deprecated method)!");
                break;

            case "uuid":
                ItemStack uuidSkull = SkullCreator.itemFromUuid(TEST_PLAYER_UUID);
                p.getInventory().addItem(uuidSkull);
                p.sendMessage("§aGiven skull from UUID!");
                break;

            case "all":
                // Test all item creation methods
                p.getInventory().addItem(SkullCreator.itemFromBase64(TEST_SKULL));
                p.getInventory().addItem(SkullCreator.itemFromUrl(TEST_URL));
                p.getInventory().addItem(SkullCreator.itemFromName(TEST_PLAYER_NAME));
                p.getInventory().addItem(SkullCreator.itemFromUuid(TEST_PLAYER_UUID));

                // Test modification methods
                ItemStack modifiableSkull = SkullCreator.createSkull();
                SkullCreator.itemWithBase64(modifiableSkull, TEST_SKULL);
                p.getInventory().addItem(modifiableSkull);

                modifiableSkull = SkullCreator.createSkull();
                SkullCreator.itemWithUrl(modifiableSkull, TEST_URL);
                p.getInventory().addItem(modifiableSkull);

                modifiableSkull = SkullCreator.createSkull();
                SkullCreator.itemWithName(modifiableSkull, TEST_PLAYER_NAME);
                p.getInventory().addItem(modifiableSkull);

                modifiableSkull = SkullCreator.createSkull();
                SkullCreator.itemWithUuid(modifiableSkull, TEST_PLAYER_UUID);
                p.getInventory().addItem(modifiableSkull);

                p.sendMessage("§aGiven 8 different skulls using all item methods!");
                break;

            default:
                p.sendMessage("§cInvalid item test option!");
                break;
        }
    }

    private void testBlockMethods(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§eBlock test options:");
            p.sendMessage("§7- /skulltest block base64 - Test base64 skull block");
            p.sendMessage("§7- /skulltest block url - Test URL skull block");
            p.sendMessage("§7- /skulltest block name - Test name skull block (deprecated)");
            p.sendMessage("§7- /skulltest block uuid - Test UUID skull block");
            p.sendMessage("§7- /skulltest block grid - Create 2x2 grid with all methods");
            return;
        }

        Block targetBlock = getTargetBlock(p, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            send(p, ChatColor.RED + "Please look at a block within 5 blocks!");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "base64":
                SkullCreator.blockWithBase64(targetBlock, TEST_SKULL);
                p.sendMessage("§aSet block to skull from base64!");
                break;

            case "url":
                SkullCreator.blockWithUrl(targetBlock, TEST_URL);
                p.sendMessage("§aSet block to skull from URL!");
                break;

            case "name":
                SkullCreator.blockWithName(targetBlock, TEST_PLAYER_NAME);
                p.sendMessage("§aSet block to skull from name (deprecated method)!");
                break;

            case "uuid":
                SkullCreator.blockWithUuid(targetBlock, TEST_PLAYER_UUID);
                p.sendMessage("§aSet block to skull from UUID!");
                break;

            case "grid":
                Location loc = targetBlock.getLocation();

                // Create 2x2 grid of skulls using different methods
                SkullCreator.blockWithBase64(loc.getBlock(), TEST_SKULL);
                SkullCreator.blockWithUrl(loc.clone().add(1, 0, 0).getBlock(), TEST_URL);
                SkullCreator.blockWithName(loc.clone().add(0, 0, 1).getBlock(), TEST_PLAYER_NAME);
                SkullCreator.blockWithUuid(loc.clone().add(1, 0, 1).getBlock(), TEST_PLAYER_UUID);

                p.sendMessage("§aCreated 2x2 skull grid using all block methods!");
                break;

            default:
                p.sendMessage("§cInvalid block test option!");
                break;
        }
    }

    private void testUtilityMethods(Player p) {
        p.sendMessage("§e=== Utility Method Tests ===");

        // Test createSkull
        ItemStack emptySkull = SkullCreator.createSkull();
        p.sendMessage("§7createSkull() - Material: " + emptySkull.getType());

        // Test reflection status
        boolean reflectionWorking = SkullCreator.isReflectionInitialized();
        p.sendMessage("§7isReflectionInitialized(): " + (reflectionWorking ? "§a✓" : "§c✗"));

        // Test cache operations
        int cacheSize = SkullCreator.getCacheSize();
        p.sendMessage("§7getCacheSize(): " + cacheSize);

        // Add some items to cache
        SkullCreator.itemFromBase64(TEST_SKULL);
        SkullCreator.itemFromUrl(TEST_URL);

        int newCacheSize = SkullCreator.getCacheSize();
        p.sendMessage("§7After creating skulls - getCacheSize(): " + newCacheSize);

        // Test cache clearing
        SkullCreator.clearCache();
        int clearedCacheSize = SkullCreator.getCacheSize();
        p.sendMessage("§7After clearCache() - getCacheSize(): " + clearedCacheSize);

        p.sendMessage("§aUtility method tests completed!");
    }

    private void runComprehensiveTest(Player p) {
        p.sendMessage("§e=== Running Comprehensive Test ===");

        long startTime = System.currentTimeMillis();

        // Test all item creation methods
        p.sendMessage("§7Testing item creation methods...");
        ItemStack[] skulls = new ItemStack[4];
        skulls[0] = SkullCreator.itemFromBase64(TEST_SKULL);
        skulls[1] = SkullCreator.itemFromUrl(TEST_URL);
        skulls[2] = SkullCreator.itemFromName(TEST_PLAYER_NAME);
        skulls[3] = SkullCreator.itemFromUuid(TEST_PLAYER_UUID);

        for (ItemStack skull : skulls) {
            p.getInventory().addItem(skull);
        }

        // Test all modification methods
        p.sendMessage("§7Testing item modification methods...");
        ItemStack modSkull1 = SkullCreator.createSkull();
        ItemStack modSkull2 = SkullCreator.createSkull();
        ItemStack modSkull3 = SkullCreator.createSkull();
        ItemStack modSkull4 = SkullCreator.createSkull();

        SkullCreator.itemWithBase64(modSkull1, TEST_SKULL);
        SkullCreator.itemWithUrl(modSkull2, TEST_URL);
        SkullCreator.itemWithName(modSkull3, TEST_PLAYER_NAME);
        SkullCreator.itemWithUuid(modSkull4, TEST_PLAYER_UUID);

        p.getInventory().addItem(modSkull1, modSkull2, modSkull3, modSkull4);

        // Test block methods if looking at a block
        Block targetBlock = getTargetBlock(p, 5);
        if (targetBlock != null && targetBlock.getType() != Material.AIR) {
            p.sendMessage("§7Testing block methods...");
            SkullCreator.blockWithBase64(targetBlock, TEST_SKULL);
        }

        // Test utility methods
        p.sendMessage("§7Testing utility methods...");
        boolean reflection = SkullCreator.isReflectionInitialized();

        final int testingSample = 1000;
        p.sendMessage("§eStarting asynchronous performance tests (" + testingSample + " iterations)...");

        final JavaPlugin plugin = this;
        final long asyncStart = System.currentTimeMillis();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Cached performance test
            long perfStart = System.currentTimeMillis();
            for (int i = 0; i < testingSample; i++) {
                String skullData = TEST_SKULLS.get(ThreadLocalRandom.current().nextInt(TEST_SKULLS.size()));
                SkullCreator.itemFromBase64(skullData);
            }
            long perfEnd = System.currentTimeMillis();

            // Cache size after cached run
            int cacheSize = SkullCreator.getCacheSize();

            // Uncached performance test
            long uncachedStart = System.currentTimeMillis();
            for (int i = 0; i < testingSample; i++) {
                String skullData = TEST_SKULLS.get(ThreadLocalRandom.current().nextInt(TEST_SKULLS.size()));
                SkullCreator.clearCache();
                SkullCreator.itemFromBase64(skullData);
            }
            long uncachedEnd = System.currentTimeMillis();

            SkullCreator.clearCache();
            long asyncEnd = System.currentTimeMillis();

            Bukkit.getScheduler().runTask(plugin, () -> {
                p.sendMessage("§a=== Comprehensive Test Results (Async) ===");
                p.sendMessage("§7Total time: " + (asyncEnd - asyncStart) + "ms");
                p.sendMessage("§7Reflection working: " + (reflection ? "§a✓" : "§c✗"));
                p.sendMessage("§7Cache size reached: " + cacheSize);
                p.sendMessage("§7Performance (cached, " + testingSample + " skulls): " + (perfEnd - perfStart) + "ms");
                p.sendMessage("§7Performance (uncached, " + testingSample + " skulls): " + (uncachedEnd - uncachedStart) + "ms");
                p.sendMessage("§7Items given: 8 skulls");
                p.sendMessage("§a✓ All tests completed successfully!");
            });
        });
    }

    /**
     * Headless variant of the comprehensive test – skips player inventory/block
     * work so it can be launched from the console or CI.
     */
    private void runComprehensiveHeadless(CommandSender out) {
        final int iterations = 1000;           // adjust for CI speed
        out.sendMessage("§e[Headless] Running comprehensive test (" + iterations + " iterations)…");

        final long asyncStart = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            boolean reflection = SkullCreator.isReflectionInitialized();

            // Cached loop
            long cachedStart = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                String data = TEST_SKULLS.get(ThreadLocalRandom.current().nextInt(TEST_SKULLS.size()));
                SkullCreator.itemFromBase64(data);
            }
            long cachedEnd = System.currentTimeMillis();

            int cacheSize = SkullCreator.getCacheSize();

            // Uncached loop
            long uncachedStart = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                String data = TEST_SKULLS.get(ThreadLocalRandom.current().nextInt(TEST_SKULLS.size()));
                SkullCreator.clearCache();
                SkullCreator.itemFromBase64(data);
            }
            long uncachedEnd = System.currentTimeMillis();

            SkullCreator.clearCache();
            long asyncEnd = System.currentTimeMillis();

            Bukkit.getScheduler().runTask(this, () -> {
                out.sendMessage("§a[Headless] Test complete!");
                out.sendMessage("§7Total time: " + (asyncEnd - asyncStart) + "ms");
                out.sendMessage("§7Reflection working: " + (reflection ? "§a✓" : "§c✗"));
                out.sendMessage("§7Cache size reached: " + cacheSize);
                out.sendMessage("§7Cached loop: " + (cachedEnd - cachedStart) + "ms");
                out.sendMessage("§7Uncached loop: " + (uncachedEnd - uncachedStart) + "ms");
                out.sendMessage("§a✓ Headless comprehensive test finished");
            });
        });
    }

    private void showInfo(Player p) {
        p.sendMessage("§e=== SkullCreator Library Info ===");
        p.sendMessage("§7Reflection initialized: " + (SkullCreator.isReflectionInitialized() ? "§a✓" : "§c✗"));
        p.sendMessage("§7Current cache size: " + SkullCreator.getCacheSize());
        p.sendMessage("§7Available methods:");
        p.sendMessage("§7  - Item creation: itemFromBase64, itemFromUrl, itemFromName, itemFromUuid");
        p.sendMessage("§7  - Item modification: itemWithBase64, itemWithUrl, itemWithName, itemWithUuid");
        p.sendMessage("§7  - Block methods: blockWithBase64, blockWithUrl, blockWithName, blockWithUuid");
        p.sendMessage("§7  - Utilities: createSkull, clearCache, getCacheSize, isReflectionInitialized");

        // Advanced reflection info
        String details = buildReflectionDetails();
        if (details != null) {
            p.sendMessage("§e--- Reflection Details ---");
            for (String line : details.split("\n")) {
                p.sendMessage("§7" + line);
            }
        }
    }

    private void showHelp(Player p) {
        send(p, ChatColor.GOLD + "=== SkullCreator Test Commands ===");
        send(p, ChatColor.GRAY + "/skulltest item <type> - Test item methods");
        send(p, ChatColor.GRAY + "/skulltest block <type> - Test block methods");
        send(p, ChatColor.GRAY + "/skulltest utility - Test utility methods");
        send(p, ChatColor.GRAY + "/skulltest comprehensive - Run all tests");
        send(p, ChatColor.GRAY + "/skulltest clear - Clear cache");
        send(p, ChatColor.GRAY + "/skulltest info - Show library info");
        send(p, ChatColor.GRAY + "/skulltest help - Show this help");
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    /**
     * Retrieves the block a player is looking at in a version-safe manner.
     * Uses {@code Player#getTargetBlockExact(int)} on 1.13+ and falls back to
     * the legacy {@code Player#getTargetBlock(Set, int)} on older versions
     * (e.g. 1.8–1.12).
     */
    private Block getTargetBlock(Player player, int maxDistance) {
        try {
            // 1.13+ – method signature: Block getTargetBlockExact(int)
            Method exact = Player.class.getMethod("getTargetBlockExact", int.class);
            return (Block) exact.invoke(player, maxDistance);
        } catch (NoSuchMethodException ignored) {
            // 1.8 – 1.12 – method signature: Block getTargetBlock(Set, int)
            try {
                Method legacy = Player.class.getMethod("getTargetBlock", Set.class, int.class);
                // Empty set = default transparent materials
                return (Block) legacy.invoke(player, null, maxDistance);
            } catch (Exception e) {
                // Should never happen, but just in case
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /* ---------------- reflection helpers ---------------- */

    private String buildReflectionDetails() {
        try {
            Class<?> resolverCls = Class.forName("com.skullcreator.internal.ProfileResolver");
            java.lang.reflect.Field applierField = resolverCls.getDeclaredField("APPLIER");
            applierField.setAccessible(true);
            Object applier = applierField.get(null);
            StringBuilder sb = new StringBuilder();
            String brand = Bukkit.getServer().getName();
            sb.append("Server brand: ").append(brand).append('\n');
            sb.append("Applier implementation: ").append(applier.getClass().getSimpleName());

            if (!applier.getClass().getSimpleName().equals("ModernApplier")) {
                return sb.append(" (generic fallback)").toString();
            }

            java.lang.reflect.Field ctxField = applier.getClass().getDeclaredField("ctx");
            ctxField.setAccessible(true);
            Object ctx = ctxField.get(applier);

            Class<?> ctxCls = Class.forName("com.skullcreator.internal.bootstrap.ReflectionContext");

            java.lang.reflect.Field gameProfileClsF = ctxCls.getDeclaredField("gameProfileClass");
            java.lang.reflect.Field propertyClsF = ctxCls.getDeclaredField("propertyClass");
            java.lang.reflect.Field setProfileF = ctxCls.getDeclaredField("setProfileMethod");
            java.lang.reflect.Field resolvableFlagF = ctxCls.getDeclaredField("useResolvableProfile");

            gameProfileClsF.setAccessible(true);
            propertyClsF.setAccessible(true);
            setProfileF.setAccessible(true);
            resolvableFlagF.setAccessible(true);

            Class<?> gpCls = (Class<?>) gameProfileClsF.get(ctx);
            Class<?> propCls = (Class<?>) propertyClsF.get(ctx);
            java.lang.reflect.Method setProfileM = (java.lang.reflect.Method) setProfileF.get(ctx);
            boolean useResolvable = (boolean) resolvableFlagF.get(ctx);

            sb.append('\n');
            sb.append("GameProfile class: ").append(gpCls.getName()).append('\n');
            sb.append("Property class:   ").append(propCls.getName()).append('\n');
            sb.append("setProfile method: ").append(setProfileM != null ? setProfileM.toGenericString() : "<none>").append('\n');
            sb.append("ResolvableProfile support: ").append(useResolvable);
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /* ---------------- test data ---------------- */

    private static final List<String> TEST_SKULLS = Arrays.asList(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2QyYjgxOGI3MzhkMDI2MDA2MGU5ZWM1MGUzZjMwNzE0NjEzZTc4OTVlNzZiMTM5OWZkODY1YWM2MTg3ZTVlZSJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFjNzQyMzE0YjY4MWEzZWM3YTljYzU0MDQ5ODczYTRhY2I3MThkNmFjZmIxNzEyMmJmYzE4YTVjYzJlOTU4YyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDFiYWNjYmY2NzZiOWZmMWY4NGQyZjEzMzM3NWJjNTVlYTdhMDNiMzg1Yjc0ODU4Y2RiMDNkZmZiNDVjN2FhZSJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzNlMzdkOWVmMDc2M2Y0YTRjZmY2ZGJhMGRkNjMxNTMwNjEwZmUyYWJkZGQ3ZWMxMDUyYWIxNmI5MDUyZDg3MCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRhZWVkMDMzZjgyMDI4MWIwNGVkMWIzOWUwOTQxZGVlNGE2MDI3MDE1MGJkMjAwODY0YzNmNTFiYTkxZjVhYiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2ViYTI5NzhkYzgxODdmNWY2ZjViMzVlMzE3ZWRiOWQ4ZDVlNzQ5YzYzOTRmMzliYjdlM2I4MGYyNWU1ZjQzNiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQyYjllYzM0OTYwNjRiMDg1ZjdkMzBjZTkwYTBjOGY3NjM2YzhlZjUzMDNiMjBjMjVjYTEwYTk5N2JkNzQzMyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzgxNDVmMDQ3ODMzNWU4OTc1NzU0YjdmZjhhY2NkZDEwNzQzMTBlM2VjNDJjMTIyNDliMDI0NTkwNjZhYmNkZCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY4NTJiNzdlZTYzNmRmODgyOWU5NTk5ZTY3YmM3ZDZiZmUzZWY2ZjNjODYzNzY0YWYxN2IxYmRmNjFmOWRlMSJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmI3NzJiMmI5YmYxMDhlZjE1ZGUxNTU2YzQxZWVjMGNjNzgxYTllNmNjNjUwNzY2OWUwZDJjM2I1NmI3NDBjYyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc3MGY2Y2NhZjg4NjFkYTNjODgxMjgyNWFmYWVjNWE5NWM1ZjMzNmY3MjhhMjZmMTM1YTI1OTRiNDg3NTQwMiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGJkYzc3OTZiYjIxNjk1OTQwZDdiODMxYzBiNzU3MjQ5ZDNiZDBmZDYzNWJmYjAxOGZmOGZhNDMxODBhNzRkYyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGM2NTBiMzZlZjAwOTk4YmY4M2QxMTViYWViNDUxMTk2MmMxMWFkYWVmYjcwOWIwMzE4YjRlMTIyOTVhN2UyIn19fQ==",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNiODYyMmE5ZWE0ZGM0NGZlYzg4MDRmYTA3NWY3NWZmN2M0ZDllNzFkZTFkZWJmZDIxZDY4Mzk3NTQzN2YzNiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODAwOTcwY2RlY2ZhMGJjNTM2NTllYTI4Zjk5NDg3MDE1NWYwZDRlMjUyNmE1MTMzOTI0NTU5MzllNTBjYzhhYiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzc3MGY2Y2NhZjg4NjFkYTNjODgxMjgyNWFmYWVjNWE5NWM1ZjMzNmY3MjhhMjZmMTM1YTI1OTRiNDg3NTQwMiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQyYjllYzM0OTYwNjRiMDg1ZjdkMzBjZTkwYTBjOGY3NjM2YzhlZjUzMDNiMjBjMjVjYTEwYTk5N2JkNzQzMyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzgxNDVmMDQ3ODMzNWU4OTc1NzU0YjdmZjhhY2NkZDEwNzQzMTBlM2VjNDJjMTIyNDliMDI0NTkwNjZhYmNkZCJ9fX0="
    );
}