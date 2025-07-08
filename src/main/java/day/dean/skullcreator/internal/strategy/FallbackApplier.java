package day.dean.skullcreator.internal.strategy;

import day.dean.skullcreator.internal.util.TexturePropertyWriter;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Public-API based implementation used when direct reflection into CraftBukkit
 * internals is not available (or failed). Relies on Bukkit/ Paper profile
 * classes only, so it should work across versions without breaking.
 */
public class FallbackApplier implements ProfileApplier {

    @Override
    public ItemStack applyToItem(ItemStack item, String base64) {
        if (!(item.getItemMeta() instanceof SkullMeta)) return item;
        try {
            // Create empty PlayerProfile via new API (>=1.20) if present
            Object playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
            TexturePropertyWriter.injectTexture(playerProfile, base64);

            SkullMeta meta = (SkullMeta) item.getItemMeta();
            try {
                // Spigot naming
                meta.setOwnerProfile((org.bukkit.profile.PlayerProfile) playerProfile);
            } catch (NoSuchMethodError | ClassCastException ignored) {
                // Older Paper naming via reflection
                Method m = meta.getClass().getMethod("setPlayerProfile", playerProfile.getClass());
                m.invoke(meta, playerProfile);
            }
            item.setItemMeta(meta);
        } catch (Exception ignored) {}
        return item;
    }

    @Override
    public void applyToBlock(Skull skull, String base64) {
        try {
            Method create = Bukkit.class.getMethod("createProfile", UUID.class, String.class);
            Object playerProfile = create.invoke(null, UUID.randomUUID(), null);
            TexturePropertyWriter.injectTexture(playerProfile, base64);

            // setPlayerProfile on CraftSkull via reflection
            Method target = null;
            for (Method m : skull.getClass().getMethods()) {
                if (m.getName().equals("setPlayerProfile") && m.getParameterCount() == 1) {
                    target = m; break;
                }
            }
            if (target != null) target.invoke(skull, playerProfile);
        } catch (Exception ignored) {}
    }
}
