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
package org.cubeengine.service.command.readers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parameter.TooFewArgumentsException;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.DefaultValue;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.cubeengine.service.command.TranslatedReaderException;
import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import static org.cubeengine.module.core.util.StringUtils.startsWithIgnoreCase;
import static org.cubeengine.service.i18n.formatter.MessageType.NEGATIVE;

public class WorldReader implements ArgumentReader<World>, DefaultValue<World>, Completer
{
    private final Game game;
    private final I18n i18n;

    public WorldReader(Game game, I18n i18n)
    {
        this.game = game;
        this.i18n = i18n;
    }

    @Override
    public World read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String name = invocation.consume(1);
        Optional<World> world = game.getServer().getWorld(name);
        if (!world.isPresent())
        {
            throw new TranslatedReaderException(i18n.translate(invocation.getContext(Locale.class), NEGATIVE, "World {input} not found!", name));
        }
        return world.get();
    }

    @Override
    public World getDefault(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof Player)
        {
            return ((Player)invocation.getCommandSource()).getWorld();
        }
        throw new TooFewArgumentsException();
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        List<String> offers = new ArrayList<>();
        for (World world : game.getServer().getWorlds())
        {
            final String name = world.getName();
            if (startsWithIgnoreCase(name, invocation.currentToken()))
            {
                offers.add(name);
            }
        }
        return offers;
    }
}
