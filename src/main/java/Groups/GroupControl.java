package Groups;

import de.cubeisland.CubeWar.Util;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Faithcaio
 */
public class GroupControl {

    Map<Integer,Group> areas = new HashMap<Integer,Group>();
    private static GroupControl instance = null;
    
    public GroupControl(ConfigurationSection config) 
    {
        for (String name : config.getKeys(false))
        {
            Group newArea = new Group();
            ConfigurationSection section = config.getConfigurationSection(name);
            if (name.equalsIgnoreCase("safezone"))
            {
                newArea.setType(AreaType.SAFEZONE);
                newArea.setIntegerValue("id", -10);
                newArea.setStringValue("name", "SafeZone");
                newArea.setStringValue("tag", "SAFE");
                newArea.setStringValue("description", "It's safe");
            }else
            if (name.equalsIgnoreCase("warland"))
            {
                newArea.setType(AreaType.WARLAND);
                newArea.setIntegerValue("id", -50);
                newArea.setStringValue("name", "WarLand");
                newArea.setStringValue("tag", "WAR");
                newArea.setStringValue("description", "War everywhere");
            }else
            if (name.equalsIgnoreCase("wildland"))
            {
                newArea.setType(AreaType.WILDLAND);
                newArea.setIntegerValue("id", 0);
                newArea.setStringValue("name", "WildLand");
                newArea.setStringValue("tag", "WILD");
                newArea.setStringValue("description", "Unclaimed Land");
            }else
            if (name.equalsIgnoreCase("team_default"))
            {
                newArea.setType(AreaType.TEAMZONE);
                newArea.setIntegerValue("id", -1);
                newArea.setStringValue("name", "TEAM_DEFAULT");
                newArea.setStringValue("tag", "Def_Team");
                newArea.setStringValue("description", "A Team");
            }else
            if (name.equalsIgnoreCase("arena_default"))
            {
                newArea.setType(AreaType.ARENA);
                newArea.setIntegerValue("id", -2);
                newArea.setStringValue("name", "ARENA_DEFAULT");
                newArea.setStringValue("tag", "Def_Arena");
                newArea.setStringValue("description", "An Arena");
            }
            if (section.getBoolean("economy.bank", false)) newArea.setBit(Group.ECONOMY_BANK);
            if (section.getBoolean("power.haspermpower"))
                newArea.setIntegerValue("power_perm", section.getInt("power.permpower"));
            else
                newArea.setIntegerValue("power_perm", null);
            newArea.setIntegerValue("power_boost", section.getInt("power.powerboost"));
            if (section.getBoolean("power.powerloss")) newArea.setBit(Group.POWER_LOSS);
            if (section.getBoolean("power.powergain")) newArea.setBit(Group.POWER_GAIN);
            if (section.getBoolean("pvp.PvP")) newArea.setBit(Group.PVP_ON);
            if (section.getBoolean("pvp.damage")) newArea.setBit(Group.PVP_DAMAGE);
            if (section.getBoolean("pvp.friendlyfire")) newArea.setBit(Group.PVP_FRIENDLYFIRE);
            newArea.setIntegerValue("pvp_spawnprotect", section.getInt("pvp.spawnprotectseconds"));
            if (section.getBoolean("monster.spawn")) newArea.setBit(Group.MONSTER_SPAWN);
            if (section.getBoolean("monster.damage")) newArea.setBit(Group.MONSTER_DAMAGE);
            if (section.getBoolean("build.destroy")) newArea.setBit(Group.BUILD_DESTROY);
            if (section.getBoolean("build.place")) newArea.setBit(Group.BUILD_PLACE);
            newArea.setListValue("protect", Util.convertListStringToMaterial(section.getStringList("protect")));
            if (section.getBoolean("use.fire")) newArea.setBit(Group.USE_FIRE);
            if (section.getBoolean("use.lava")) newArea.setBit(Group.USE_LAVA);
            if (section.getBoolean("use.water")) newArea.setBit(Group.USE_WATER);
            newArea.setListValue("denycommands", section.getStringList("denycommands"));
            
            areas.put(newArea.getId(), newArea);
        }
    }
    
    public Group newTeam(String tag, String name)
    {
        Group newArea = areas.get(-1).clone();
        newArea.setStringValue("tag", tag);
        newArea.setStringValue("name", name);
        //TODO DATABASE Get ID!!!!!!!
        int id = areas.size()-4;
        newArea.setIntegerValue("id", id);
        areas.put(id, newArea);
        //#############################
        return newArea;
    }
    
    public Group newArena(String tag, String name)
    {
        Group newArea = areas.get(-2).clone();
        newArea.setStringValue("tag", tag);
        newArea.setStringValue("name", name);
        //TODO DATABASE Get ID!!!!!!!
        int id = areas.size()-4;
        newArea.setIntegerValue("id", id);
        areas.put(id, newArea);
        //#############################
        return newArea;
    }
    
    public static void createInstance(ConfigurationSection config)
    {
       instance = new GroupControl(config);
    }
    
    public static GroupControl get()
    {
        return instance;
    }
    
    public Integer getTeamGroup(String tag)
    {
        for (Group area : areas.values())
        {
            if (area.getTag().equalsIgnoreCase(tag))
                if (area.getType().equals(AreaType.TEAMZONE))
                    return area.getId();
        }    
        return null;
    }
    
    public Integer getArenaGroup(String tag)
    {
        for (Group area : areas.values())
        {
            if (area.getTag().equalsIgnoreCase(tag))
                if (area.getType().equals(AreaType.ARENA))
                    return area.getId();
        }
        return null;
    }
    
    public boolean setGroupValue(int id, String key, String value)
    {
        Group area = areas.get(id);
        return area.setValue(key, value);
    }
    
    public Group getGroup(int id)
    {
        return areas.get(id);
    }
    
    public Group getGroup(String tag)
    {
        for (Group area : areas.values())
        {
            if (area.getTag().equalsIgnoreCase(tag))
                return area;
        }
        return null;
    }

    public boolean freeTag(String tag)
    {
        if (this.getGroup(tag)==null) return true;
        else return false;
    }
    
    public int getRank(Group gruop)
    {
        return 1;
        //TODO ausrechnen
    }
}