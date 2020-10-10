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
package org.cubeengine.libcube.service.command.example;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.cubeengine.libcube.service.command.Command;
import org.cubeengine.libcube.service.command.DispatcherCommand;
import org.spongepowered.api.command.CommandCause;

@Command(name = "example", desc = "base command")
public class ParentExampleCommand extends DispatcherCommand {

    @Inject
    public ParentExampleCommand(ChildExampleCommand1 cmd1, ChildExampleCommand2 cmd2) {
        super(cmd1, cmd2);
    }

    @Command(desc = "Does a thing")
    public void myCommand(CommandCause cause, String firstParam) {
        cause.sendMessage(Component.text(firstParam));
    }
}
