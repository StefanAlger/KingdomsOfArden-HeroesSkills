
package net.swagserv.andrew2060.heroes.skills;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LargeFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;

public class SkillFireball extends ActiveSkill {


	private Map<LargeFireball, FireballData> fireballs = new LinkedHashMap<LargeFireball, FireballData>(100) {
        private static final long serialVersionUID = 4329526013158603250L;
    };
    private class FireballData {
    	long creationtime;
    	Player player;
    	FireballData(long creationtime, Player player) {
    		this.creationtime = creationtime;
    		this.player = player;
    	}
    }
    
    public SkillFireball(Heroes plugin) {
        super(plugin, "Fireball");
        setDescription("You shoot a fireball that deals $1 damage!");
        setUsage("/skill fireball");
        setArgumentRange(0, 0);
        setIdentifiers("skill fireball");
        setTypes(SkillType.FIRE, SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.HARMFUL);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.DAMAGE.node(), 4);
        node.set(Setting.DAMAGE_INCREASE.node(), 0.0);
        node.set("velocity-multiplier", 0.5);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        LargeFireball fireball = player.launchProjectile(LargeFireball.class);
        fireball.setIsIncendiary(false);
        fireball.setYield(0F);
        fireballs.put(fireball, new FireballData(System.currentTimeMillis(),player));
        double mult = SkillConfigManager.getUseSetting(hero, this, "velocity-multiplier", 1.5, false);
        fireball.setVelocity(fireball.getVelocity().multiply(mult));
        fireball.setShooter(player);
        broadcastExecuteText(hero); 
        return SkillResult.NORMAL;
    }

    public class SkillEntityListener implements Listener {

        private final Skill skill;

        public SkillEntityListener(Skill skill) {
            this.skill = skill;
        }

        @EventHandler()
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity)) {
                return;
            }

            EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
            Entity fireball = subEvent.getDamager();
            if (!(fireball instanceof LargeFireball) || !fireballs.containsKey(fireball)) {
                return;
            }
            Player dmger = fireballs.get(fireball).player;
            fireballs.remove(fireball);
            LivingEntity entity = (LivingEntity) subEvent.getEntity();
            Hero hero = plugin.getCharacterManager().getHero(dmger);
            event.setCancelled(true);
            if(!Skill.damageCheck(dmger, entity)) {
            	return;
            }
            addSpellTarget(entity, hero);
            int damage = SkillConfigManager.getUseSetting(hero, skill, Setting.DAMAGE, 4, false);
            damage += (int) (SkillConfigManager.getUseSetting(hero, skill, Setting.DAMAGE_INCREASE, 0.0, false) * hero.getSkillLevel(skill));
            damageEntity(entity, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
        }
        @EventHandler(priority = EventPriority.LOWEST)
        public void onGhastProjectileHit(EntityExplodeEvent event) {
        	if(event.isCancelled() || !(event.getEntity() instanceof LargeFireball)) {
        		return;
        	}
        	LargeFireball fireball = (LargeFireball) event.getEntity();
        	if(fireballs.containsKey(fireball)) {
        		fireballs.remove(fireball);
        		event.setCancelled(true);
        	}
        }
        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onProjectileHit(ProjectileHitEvent event) {
        	if(!(event.getEntity() instanceof LargeFireball)) {
        		return;
        	}
        	LargeFireball fireball = (LargeFireball)event.getEntity();
        	if(!fireballs.containsKey(fireball)) {
        		return;
        	}
        	fireballs.remove(fireball);
        	fireball.setIsIncendiary(false);
        	fireball.setFireTicks(0);
        }
    }

    @Override
    public String getDescription(Hero hero) {
        int damage = SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE, 1, false);
        damage += (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE_INCREASE, 0.0, false) * hero.getSkillLevel(this));
        return getDescription().replace("$1", damage + "");
    }
}