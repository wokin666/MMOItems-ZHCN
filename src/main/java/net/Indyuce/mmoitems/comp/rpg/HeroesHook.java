package net.Indyuce.mmoitems.comp.rpg;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillUseInfo;
import com.herocraftonline.heroes.api.events.ClassChangeEvent;
import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillType;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageHandler;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.RegisteredAttack;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HeroesHook implements RPGHandler, Listener, DamageHandler {
	private final Map<SkillType, DamageType> damages = new HashMap<>();

	public HeroesHook() {
		MMOLib.plugin.getDamage().registerHandler(this);

		damages.put(SkillType.ABILITY_PROPERTY_PHYSICAL, DamageType.PHYSICAL);
		damages.put(SkillType.ABILITY_PROPERTY_MAGICAL, DamageType.MAGIC);
		damages.put(SkillType.ABILITY_PROPERTY_PROJECTILE, DamageType.PROJECTILE);
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return Heroes.getInstance().getDamageManager().isSpellTarget(entity);
	}

	@Override
	public RegisteredAttack getDamage(Entity entity) {
		SkillUseInfo info = Heroes.getInstance().getDamageManager().getSpellTargetInfo(entity);
		return new RegisteredAttack(new AttackResult(true, 0, info.getSkill().getTypes().stream().filter(damages::containsKey)
				.map(damages::get).collect(Collectors.toSet())), info.getCharacter().getEntity());
	}

	@Override
	public void refreshStats(PlayerData data) {
		Hero hero = ((HeroesPlayer) data.getRPG()).hero;
		hero.removeMaxMana("MMOItems");
		hero.addMaxMana("MMOItems", (int) data.getStats().getStat(ItemStats.MAX_MANA));
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new HeroesPlayer(data);
	}

	/*
	 * update the player's inventory whenever he levels up since it could change
	 * its current stat requirements
	 */
	@EventHandler
	public void a(HeroChangeLevelEvent event) {
		PlayerData.get(event.getHero().getPlayer()).getInventory().scheduleUpdate();
	}

	@EventHandler
	public void b(ClassChangeEvent event) {
		PlayerData.get(event.getHero().getPlayer()).getInventory().scheduleUpdate();
	}

//	@EventHandler
//	public void b(SkillDamageEvent event) {
//
//		/*
//		 * apply the 'Magic Damage' and 'Magic Damage Reduction' item option to
//		 * Heroes skills
//		 */
//		if (event.getSkill().isType(SkillType.ABILITY_PROPERTY_MAGICAL)) {
//			if (event.getDamager().getEntity() instanceof Player)
//				event.setDamage(event.getDamage() * (1 + PlayerData.get((Player) event.getDamager().getEntity()).getStats().getStat(ItemStat.MAGIC_DAMAGE) / 100));
//			if (event.getEntity() instanceof Player)
//				event.setDamage(event.getDamage() * (1 - PlayerData.get((Player) event.getDamager().getEntity()).getStats().getStat(ItemStat.MAGIC_DAMAGE_REDUCTION) / 100));
//		}
//
//		/*
//		 * apply 'Physical Damage Reduction' to physical skills
//		 */
//		if (event.getSkill().isType(SkillType.ABILITY_PROPERTY_PHYSICAL))
//			if (event.getEntity() instanceof Player)
//				event.setDamage(event.getDamage() * (1 - PlayerData.get((Player) event.getDamager().getEntity()).getStats().getStat(ItemStat.PHYSICAL_DAMAGE_REDUCTION) / 100));
//	}

	public static class HeroesPlayer extends RPGPlayer {
		private final Hero hero;

		public HeroesPlayer(PlayerData playerData) {
			super(playerData);

			hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
		}

		@Override
		public int getLevel() {
			return hero.getHeroLevel();
		}

		@Override
		public String getClassName() {
			return hero.getHeroClass().getName();
		}

		@Override
		public double getMana() {
			return hero.getMana();
		}

		@Override
		public double getStamina() {
			return hero.getStamina();
		}

		@Override
		public void setMana(double value) {
			hero.setMana((int) value);
		}

		@Override
		public void setStamina(double value) {
			hero.setStamina((int) value);
		}
	}
}