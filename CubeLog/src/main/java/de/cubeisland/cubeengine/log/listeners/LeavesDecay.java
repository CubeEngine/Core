package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeavesDecay extends LogListener
{
    public LeavesDecay(Log module)
    {
        super(module, new DecayConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        //TODO
    }

    public static class DecayConfig extends LogSubConfiguration
    {
        public DecayConfig()
        {
            this.actions.put(LogAction.LEAVESDECAY, false);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "decay";
        }
    }
}