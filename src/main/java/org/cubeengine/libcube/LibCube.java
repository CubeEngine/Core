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
package org.cubeengine.libcube;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.logging.log4j.Logger;
import org.cubeengine.libcube.service.command.annotation.ModuleCommand;
import org.cubeengine.libcube.service.command.example.ParentExampleCommand;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.cubeengine.processor.Core;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.nio.file.Path;
/*
Sponge gripes

commands: support for handling incomplete commands

? CommandContext or Cause helper isPlayer? isConsole?
? CommandContext or Cause helper getLocale
async tab-complete
commands: console command audience is not SystemSubject
commands: flags repeat
commands: errors are not shown?

translations for various types (TranslationTextComponent LanguageMap)
enchantment data allowed?
Adventure: Audience with permissions
Keys.IS_REPAIRABLE

inventory transaction drop excess items

SpawnEgg getEntityType and getFor EntityType

PlayerChatEvent no way to get original player receiving

 */
@Core
public class LibCube
{
    private final File path;
    private final Logger pluginLogger;
    private PluginContainer container;
    private ModuleManager mm;

    @ModuleCommand private ParentExampleCommand cmd;

    @Inject
    public LibCube(@ConfigDir(sharedRoot = true) Path path, Logger logger, Injector injector, PluginContainer container)
    {
        this.path = path.resolve("cubeengine").toFile();
        this.pluginLogger = logger;
        this.container = container;
        this.mm = new ModuleManager(this.path, logger, this, container, injector);
    }

    @Listener
    public void onConstructed(LoadedGameEvent event)
    {
        this.mm.init();
    }

    public ModuleManager getModuleManager()
    {
        return mm;
    }

    public PluginContainer getContainer() {
        return container;
    }
}
