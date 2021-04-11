package net.Indyuce.mmoitems.api.crafting.ingredient;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.LegacyComponent;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VanillaIngredient extends Ingredient {
	private final Material material;

	/**
	 * displayName is the itemMeta display name; display corresponds to how the
	 * ingredient displays in the crafting recipe GUI item lore
	 */
	private final String displayName, display;

	public VanillaIngredient(MMOLineConfig config) {
		super("vanilla", config);

		config.validate("type");

		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		displayName = config.contains("name") ? config.getString("name") : null;

		display = config.contains("display") ? config.getString("display") : MMOUtils.caseOnWords(material.name().toLowerCase().replace("_", " "));
	}

	@Override
	public String getKey() {
		return "vanilla:" + material.name().toLowerCase() + "_" + displayName;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#item#", display).replace("#amount#", "" + getAmount());
	}

	@NotNull
    @Override
	public ItemStack generateItemStack(@NotNull RPGPlayer player) {
		NBTItem item = NBTItem.get(new ItemStack(material, getAmount()));
		if (displayName != null) {
			item.setDisplayNameComponent(LegacyComponent.parse(displayName));
		}
		return item.toItem();
	}
}
