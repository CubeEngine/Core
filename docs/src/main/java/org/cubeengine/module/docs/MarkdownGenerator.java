/*
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
package org.cubeengine.module.docs;

import de.cubeisland.engine.logscribe.Log;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.StringUtils;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.alias.AliasConfiguration;
import org.cubeengine.libcube.service.command.CubeDescriptor;
import org.cubeengine.libcube.service.command.HelpCommand;
import org.cubeengine.libcube.service.permission.Permission;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.plugin.meta.PluginDependency;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class MarkdownGenerator implements Generator {

    public String generate(Log log, String name, PluginContainer pc, Info info, Set<Permission> permissions, Set<CommandBase> commands, Permission basePermission)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(name);
        if (info.workInProgress)
        {
            sb.append(" [WIP]");
        }

        sb.append("\n");
        sb.append(pc.getDescription().orElse(""));
        sb.append("\n");
        if (info.features.isEmpty())
        {
            log.warn("Missing Features for " + name + "(" + pc.getId() + ")!");
        }
        else
        {
            sb.append("\n## Features:\n");
            for (String feature : info.features)
            {
                sb.append(" - ").append(feature).append("\n");
            }
        }

        Set<PluginDependency> plugDep = pc.getDependencies();
        if (plugDep.size() > 2) // ignore cubeengine-core and spongeapi
        {
            sb.append("\n## Dependencies:\n");
            for (PluginDependency dep : plugDep)
            {
                if (dep.getId().equals("cubeengine-core") || dep.getId().equals("spongeapi"))
                {
                    continue;
                }
                // TODO link to module or plugin on ore if possible?
                sb.append(" `").append(dep.getId()).append("`");
            }
            sb.append("\n");
        }

        TreeMap<String, Permission> addPerms = new TreeMap<>(permissions.stream().collect(toMap(Permission::getId, p -> p)));
        if (!commands.isEmpty())
        {
            sb.append("\n## Commands:").append("\n\n");

            sb.append("| Command | Description | Permission<br>`").append(basePermission.getId()).append(".command.<perm>`").append(" |\n");
            sb.append("| --- | --- | --- |\n");
            for (CommandBase command : commands)
            {
                generateCommandDocs(sb, addPerms, command, new Stack<>(), basePermission, true);
            }

            for (CommandBase command : commands)
            {
                generateCommandDocs(sb, addPerms, command, new Stack<>(), basePermission, false);
            }

        }

        if (!addPerms.values().isEmpty())
        {
            sb.append("\n## Additional Permissions:\n\n");
            sb.append("| Permission | Description |\n");
            sb.append("| --- | --- |\n");
            for (Permission perm : addPerms.values())
            {
                sb.append("| `").append(perm.getId()).append("` | ").append(perm.getDesc()).append(" |");
            }
        }

        return sb.toString();
    }

    private void generateCommandDocs(StringBuilder sb, Map<String, Permission> addPerms, CommandBase command, Stack<String> commandStack, Permission basePermission, boolean overview)
    {

        if (command instanceof AliasCommand || command instanceof HelpCommand)
        {
            return;
        }
        String id = basePermission.getId() + ".command.";

        List<CommandBase> subCommands = command instanceof Dispatcher ? new ArrayList<>(((Dispatcher) command).getCommands()) : Collections.emptyList();
        subCommands.sort(Comparator.comparing(o -> o.getDescriptor().getName()));

        if (overview)
        {
            commandStack.push("*" + command.getDescriptor().getName() + "*");
            String fullCmd = StringUtils.join(" ", commandStack);
            sb.append("| [").append(fullCmd).append("]")
              .append("(#").append(fullCmd.replace("*", "").replace(" ", "-").toLowerCase()).append(") | ");
            sb.append(command.getDescriptor().getDescription()).append(" | ");
            Permission perm = ((CubeDescriptor) command.getDescriptor()).getPermission().getRegistered();
            sb.append("`").append(perm.getId().replace(id, "")).append("` |\n");

            commandStack.pop();
            commandStack.push("**" + command.getDescriptor().getName() + "**");

        }
        else
        {
            commandStack.push(command.getDescriptor().getName());
            String fullCmd = StringUtils.join(" ", commandStack);
            sb.append("\n#### ").append(fullCmd).append("  \n");
            sb.append(command.getDescriptor().getDescription()).append("  \n");
            sb.append("**Usage:** `").append(command.getDescriptor().getUsage(null)).append("`  \n");

            if (!command.getDescriptor().getAliases().isEmpty())
            {
                sb.append("**Alias:**");
                for (AliasConfiguration alias : command.getDescriptor().getAliases())
                {
                    String[] dispatcher = alias.getDispatcher();
                    List<String> labels = new ArrayList<>();
                    if (dispatcher == null)
                    {
                        labels.add(alias.getName()); // local alias
                    }
                    else
                        {
                        labels.addAll(Arrays.asList(alias.getDispatcher()));
                        labels.add(alias.getName());
                        labels.set(0, "/" + labels.get(0));
                    }
                    sb.append(" `").append(StringUtils.join(" ", labels)).append("`");
                }
                sb.append("  \n");
            }

            if (command.getDescriptor() instanceof CubeDescriptor)
            {
                Permission perm = ((CubeDescriptor) command.getDescriptor()).getPermission().getRegistered();
                sb.append("**Permission:** `").append(perm.getId()).append("`  \n");
                addPerms.remove(perm.getId());

                // TODO parameter permission?
                // TODO parameter description?
                // TODO Butler Parser with default parameter descriptions
            }
        }

        if (!overview)
        {
            StringBuilder subBuilder = new StringBuilder();
            for (CommandBase sub : subCommands)
            {
                if (!(sub instanceof HelpCommand) && !(sub instanceof AliasCommand))
                {
                    subBuilder.append(" `").append(sub.getDescriptor().getName()).append("`");
                }
            }

            if (subBuilder.length() != 0)
            {
                sb.append("**SubCommands:**").append(subBuilder.toString());
            }
            sb.append("  \n");
        }

        for (CommandBase sub : subCommands)
        {
            this.generateCommandDocs(sb, addPerms, sub, commandStack, basePermission, overview);
        }

        commandStack.pop();
    }
}
