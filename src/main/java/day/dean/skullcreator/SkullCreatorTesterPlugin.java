package day.dean.skullcreator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Comprehensive test plugin for SkullCreator library.
 * Tests all available functions and methods.
 *
 * @author deanveloper on 12/28/2016.
 */
public class SkullCreatorTesterPlugin extends JavaPlugin {

	private static final String TEST_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDllY2NjNWMxYzc5YWE3ODI2YTE1YTdmNWYxMmZiNDAzMjgxNTdjNTI0MjE2NGJhMmFlZjQ3ZTVkZTlhNWNmYyJ9fX0=";
	private static final String TEST_URL = "http://textures.minecraft.net/texture/955d611a878e821231749b2965708cad942650672db09e26847a88e2fac2946";
	private static final String TEST_PLAYER_NAME = "diamondpixel";
	private static final UUID TEST_PLAYER_UUID = UUID.fromString("7cdf75c8-68dd-4aca-8706-d8137ad03d02"); // diamondpixel's UUID


	public void onEnable() {
		if (!getDescription().getVersion().endsWith("SNAPSHOT")) {
			throw new IllegalStateException("This is not intended to run as a plugin!");
		} else {
			Bukkit.getLogger().info("SkullCreator Test Plugin Loaded!");



			// Test reflection initialization
			if (SkullCreator.isReflectionInitialized()) {
				Bukkit.getLogger().info("✓ Reflection initialized successfully!");
			} else {
				Bukkit.getLogger().warning("✗ Reflection initialization failed! \n");
			}

			// Display initial cache size
			Bukkit.getLogger().info("Initial cache size: " + SkullCreator.getCacheSize());
		}


	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String lbl, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be used by players!");
			return true;
		}

		Player p = (Player) sender;

		if (args.length == 0) {
			showHelp(p);
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
				case "comprehensive":
					runComprehensiveTest(p);
					break;
				case "clear":
					SkullCreator.clearCache();
					p.sendMessage("§aCache cleared! New size: " + SkullCreator.getCacheSize());
					break;
				case "info":
					showInfo(p);
					break;
				case "testskull":
				{
					ItemStack skull = SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJhM2Q0ZjhkYjQyNGUxNGI3NDQzZjVlNjI1NzQwOGI3NGZkNGEyNzVkNjE3OTgyNzJhZDhlNjVlMjMxOGZmMyJ9fX0=");
					p.getInventory().addItem(skull);
					p.sendMessage("Test skull added!");
					break;
				}
				default:
					showHelp(p);
					break;
			}
		} catch (Exception e) {
			p.sendMessage("§cError executing command: " + e.getMessage());
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

		Block targetBlock = p.getTargetBlock(null, 5);
		if (targetBlock == null || targetBlock.getType() == Material.AIR) {
			p.sendMessage("§cPlease look at a block to place the skull on!");
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
		Block targetBlock = p.getTargetBlock(null, 5);
		if (targetBlock != null && targetBlock.getType() != Material.AIR) {
			p.sendMessage("§7Testing block methods...");
			SkullCreator.blockWithBase64(targetBlock, TEST_SKULL);
		}

		// Test utility methods
		p.sendMessage("§7Testing utility methods...");
		boolean reflection = SkullCreator.isReflectionInitialized();
		int cacheSize = SkullCreator.getCacheSize();

		// Performance test
		p.sendMessage("§7Running performance test...");
		long perfStart = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			SkullCreator.itemFromBase64(TEST_SKULL);
		}
		long perfEnd = System.currentTimeMillis();

		SkullCreator.clearCache();

		long endTime = System.currentTimeMillis();

		p.sendMessage("§a=== Comprehensive Test Results ===");
		p.sendMessage("§7Total time: " + (endTime - startTime) + "ms");
		p.sendMessage("§7Reflection working: " + (reflection ? "§a✓" : "§c✗"));
		p.sendMessage("§7Cache size reached: " + cacheSize);
		p.sendMessage("§7Performance (100 skulls): " + (perfEnd - perfStart) + "ms");
		p.sendMessage("§7Items given: 8 skulls");
		p.sendMessage("§a✓ All tests completed successfully!");
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
	}

	private void showHelp(Player p) {
		p.sendMessage("§e=== SkullCreator Test Commands ===");
		p.sendMessage("§7/skulltest item <type> - Test item methods");
		p.sendMessage("§7/skulltest block <type> - Test block methods");
		p.sendMessage("§7/skulltest utility - Test utility methods");
		p.sendMessage("§7/skulltest comprehensive - Run all tests");
		p.sendMessage("§7/skulltest clear - Clear cache");
		p.sendMessage("§7/skulltest info - Show library info");
		p.sendMessage("§7/skulltest help - Show this help");
	}
}