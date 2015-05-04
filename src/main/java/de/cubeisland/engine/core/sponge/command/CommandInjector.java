/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.sponge.command;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import de.cubeisland.engine.butler.CommandBase;
import de.cubeisland.engine.butler.CommandDescriptor;
import de.cubeisland.engine.butler.Dispatcher;
import de.cubeisland.engine.butler.alias.AliasDescriptor;
import de.cubeisland.engine.core.sponge.SpongeCore;
import de.cubeisland.engine.core.sponge.BukkitCoreConfiguration;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeDescriptor;
import de.cubeisland.engine.core.command.HelpCommand;
import de.cubeisland.engine.core.module.Module;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.util.command.CommandMapping;

/**
 * Injects CubeEngine commands directly into Bukkits CommandMap
 */
public class CommandInjector
{
    protected final SpongeCore core;
    private final CommandService dispatcher;
    private Field knownCommandField;

    @SuppressWarnings("unchecked")
    public CommandInjector(SpongeCore core)
    {
        this.core = core;
        this.dispatcher = core.getGame().getCommandDispatcher();
    }

    protected synchronized Map<String, Collection<CommandMapping>> getKnownCommands()
    {
        return dispatcher.getAll().asMap();
    }

    public synchronized void registerCommand(CommandBase command)
    {
        WrappedCommand newCommand = new WrappedCommand(command);
        SimpleCommandMap commandMap = getCommandMap();
        Command old = this.getCommand(command.getDescriptor().getName());
        if (old != null)
        {
            BukkitCoreConfiguration config = this.core.getConfiguration();
            if (!config.commands.noOverride.contains(old.getLabel().toLowerCase(Locale.ENGLISH)))
            {
                // CE commands are more important :P
                this.removeCommand(old.getLabel(), false);
                String fallbackPrefix = core.getConfiguration().defaultFallback;
                if (old instanceof PluginCommand)
                {
                    fallbackPrefix = ((PluginCommand)old).getPlugin().getName();
                }
                else if (old instanceof VanillaCommand)
                {
                    fallbackPrefix = "vanilla";
                }
                else if (old instanceof WrappedCommand)
                {
                    fallbackPrefix = ((WrappedCommand)old).getModule().getId();
                }
                getKnownCommands().put(fallbackPrefix + ":" + newCommand.getLabel(), newCommand);
                newCommand.register(commandMap);
            }// sometimes they are not :(
        }
        commandMap.register(newCommand.getModule().getId(), newCommand);
        WrappedCommandHelpTopic topic = new WrappedCommandHelpTopic(newCommand);
        newCommand.setHelpTopic(topic);
        if (helpTopicMap != null)
        {
            this.helpTopicMap.put(topic.getName(), topic);
        }
    }

    public Command getCommand(String name)
    {
        return getCommandMap().getCommand(name);
    }

    public boolean dispatchCommand(CommandSender sender, String commandLine)
    {
        return getCommandMap().dispatch(sender, commandLine);
    }

    public void removeCommand(String name, boolean completely)
    {
        Map<String, Command> knownCommands = this.getKnownCommands();
        Command removed = knownCommands.remove(name.toLowerCase());
        if (removed != null)
        {
            Iterator<Entry<String, Command>> it = knownCommands.entrySet().iterator();
            Command next;
            boolean hasAliases = false;
            while (it.hasNext())
            {
                next = it.next().getValue();
                if (next == removed)
                {
                    hasAliases = true;
                    if (completely)
                    {
                        it.remove();
                    }
                }
            }
            if (hasAliases)
            {
                removed.unregister(getCommandMap());
            }
        }

        if (removed instanceof WrappedCommand)
        {
            if (helpTopicMap != null)
            {
                this.helpTopicMap.values().remove(((WrappedCommand)removed).getHelpTopic());
            }
        }
    }

    public void removeCommands(Module module)
    {
        for (Command command : new HashSet<>(getCommandMap().getCommands()))
        {
            if (command instanceof WrappedCommand)
            {
                if (((WrappedCommand)command).getModule() == module)
                {
                    this.removeCommand(command.getLabel(), true);
                }
                else
                {
                    this.removeSubCommands(module, ((WrappedCommand)command).getCommand());
                }
            }
        }
    }

    private void removeSubCommands(Module module, CommandBase command)
    {
        if (!(command instanceof Dispatcher))
        {
            return;
        }
        Set<CommandBase> commands = ((Dispatcher)command).getCommands();
        if (commands.isEmpty())
        {
            return;
        }
        Iterator<CommandBase> it = commands.iterator();

        while (it.hasNext())
        {
            CommandBase subCmd = it.next();
            if (subCmd instanceof HelpCommand)
            {
                continue;
            }
            CommandDescriptor descriptor = subCmd.getDescriptor();
            if (descriptor instanceof AliasDescriptor)
            {
                descriptor = ((AliasDescriptor)descriptor).mainDescriptor();
            }
            if (((CubeDescriptor)descriptor).getModule() == module)
            {
                it.remove();
            }
            else
            {
                this.removeSubCommands(module, subCmd);
            }
        }
    }

    public void removeCommands()
    {
        for (Command command : new HashSet<>(getCommandMap().getCommands()))
        {
            if (command instanceof WrappedCommand)
            {
                this.removeCommand(command.getLabel(), true);
            }
        }
    }

    public void shutdown()
    {
        this.removeCommands();
        this.commandMap = null;
        this.knownCommandField = null;
    }
}
