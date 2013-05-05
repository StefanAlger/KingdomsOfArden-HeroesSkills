package net.swagserv.andrew2060.heroes.skills;


import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.swagserv.andrew2060.heroes.skills.turretModules.TurretEffect;
import net.swagserv.andrew2060.heroes.skills.turretModules.TurretFireWrapper;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;

public class SkillCommandSlow extends ActiveSkill {

	public SkillCommandSlow(Heroes plugin) {
		super(plugin, "CommandSlow");
		setDescription("Command: Slow: Turrets will slow all enemies in range starting at a 15% slow. For each additional firing cycle that one remains in the turrets radius, the effect of the slow will be increased by 15% up to a maximum of 90%");
		setUsage("/skill commandslow");
		setArgumentRange(0,0);
		setIdentifiers("skill commandslow");
	}

	@Override
	public SkillResult use(Hero h, String[] arg1) {
		if(h.hasEffect("TurretEffectCooldown")) {
			h.getPlayer().sendMessage("You must wait 10 seconds between using different command skills!");
			return SkillResult.NORMAL;
		}
		h.addEffect(new ExpirableEffect(this,this.plugin,"TurretEffectCooldown",10000));
		TurretEffect tE;
		if(!h.hasEffect("TurretEffect")) {
			tE = new TurretEffect(plugin, this);
			h.addEffect(tE);
		} else {
			tE = (TurretEffect)h.getEffect("TurretEffect");
		}
		SlowTurret fireFunc = new SlowTurret();
		tE.setFireFunctionWrapper(fireFunc);
		return SkillResult.NORMAL;
	}
	private class SlowTurret extends TurretFireWrapper {
		int slowlevel;

		@Override
		public void fire(Hero h, Location loc, double range) {
			Arrow a = loc.getWorld().spawnArrow(loc, new Vector(0,0,0), 0.6f, 1.6f);
			Iterator<Entity> nearby = a.getNearbyEntities(range*2, range*2, range*2).iterator();

			while(nearby.hasNext()) {
				Entity next = nearby.next();
				if(!(next instanceof LivingEntity)) {
					continue;
				}
				if(Skill.damageCheck(h.getPlayer(), (LivingEntity) next) && (LivingEntity)next != h.getEntity()) {

					Iterator<PotionEffect> effects = ((LivingEntity)next).getActivePotionEffects().iterator();
					slowlevel = 1;
					while(effects.hasNext()) {
						PotionEffect nextEffect = effects.next();
						if(nextEffect.getType().equals(PotionEffectType.SLOW)) {
							slowlevel += nextEffect.getAmplifier();
							break;
						}
					}
					if(slowlevel > 5) {
						slowlevel = 5;
					}
					((LivingEntity)next).addPotionEffect(PotionEffectType.SLOW.createEffect(100, slowlevel));
				}
			}
			a.remove();
			return;
		}
	}
	@Override
	public String getDescription(Hero h) {
		return getDescription();
	}

}
