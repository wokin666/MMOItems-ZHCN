package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class KnockbackResistance extends AttributeStat {
	public KnockbackResistance() {
		super("KNOCKBACK_RESISTANCE", new ItemStack(Material.CHAINMAIL_CHESTPLATE), "Knockback Resistance", new String[] { "The chance of your item to block the", "knockback from explosions, creepers...", "1.0 corresponds to 100%, 0.7 to 70%..." }, Attribute.GENERIC_KNOCKBACK_RESISTANCE);
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag("MMOITEMS_KNOCKBACK_RESISTANCE", value));
		item.getLore().insert("knockback-resistance", format(value, "#", new StatFormat("#").format(value * 100)));
	}
}