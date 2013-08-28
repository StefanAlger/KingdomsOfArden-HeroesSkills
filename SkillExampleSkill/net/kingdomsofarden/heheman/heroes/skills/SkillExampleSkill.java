package net.kingdomsofarden.heheman.heroes.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
//ActiveSkill, PassiveSkill, and TargettedSkill are the valid types.
public class SkillExampleSkill extends ActiveSkill {

	//This is where you define the name, and description
	public SkillExampleSkill(Heroes plugin) {
		super(plugin, "ExampleSkill");
		setDescription("This is an example skill. Your Hero's level is $1");
		setIdentifiers("skill exampleskill"); //This is how heroes finds the skill in question when you use /skill skillname
		setUsage("/skill exampleskill");	//NOt sure what this is, but its required. 
		setArgumentRange(0,0);	//Valid argument range, parameters are min/max->i.e. /skill exampleskill has noarguments, /skill exampleskill targetname has one argument
	}

	//This is what gets called when people use /skill exampleskill->do whatever here
	@Override
	public SkillResult use(Hero hero, String[] args) {
		hero.getPlayer().sendMessage("You Used the Example Skill!");
		return SkillResult.NORMAL;
	}

	//This is your opportunity to replace user specific info in the description
	@Override
	public String getDescription(Hero hero) {
		int heroLevel = hero.getLevel();
		return getDescription().replace("$1", heroLevel + "");
		//Igenerally use $number because you WON'T find it being casually used/replaced.
	}
}
