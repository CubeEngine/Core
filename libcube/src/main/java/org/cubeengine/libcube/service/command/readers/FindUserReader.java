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
package org.cubeengine.libcube.service.command.readers;

import java.util.Locale;
import java.util.Optional;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.cubeengine.libcube.service.command.TranslatedReaderException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.matcher.UserMatcher;
import org.spongepowered.api.entity.living.player.User;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;

/**
 * This argument is used to get users
 */
public class FindUserReader implements ArgumentReader<User>
{
    private final I18n i18n;
    private UserMatcher um;

    public FindUserReader(I18n i18n, UserMatcher um)
    {
        this.i18n = i18n;
        this.um = um;
    }

    @Override
    public User read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String arg = invocation.consume(1);
        Optional<User> user = um.match(arg, true);
        if (user.isPresent())
        {
            throw new TranslatedReaderException(i18n.getTranslation(invocation.getContext(Locale.class), NEGATIVE, "Player {user} not found!", arg));
        }
        return user.get();
    }
}
