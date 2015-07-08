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
package de.cubeisland.engine.module.core.util.converter;

import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.converter.SimpleConverter;
import de.cubeisland.engine.converter.node.Node;
import de.cubeisland.engine.converter.node.StringNode;
import de.cubeisland.engine.service.user.SpongeUserManager;
import de.cubeisland.engine.service.user.UserManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.service.user.UserStorage;

public class PlayerConverter extends SimpleConverter<User>
{
    private final UserStorage userStorage;
    private UserManager um;

    public PlayerConverter(UserManager um, Game game)
    {
        this.um = um;
        this.userStorage = game.getServiceManager().provide(UserStorage.class).orNull();
    }

    @Override
    public Node toNode(User object)
    {
        return StringNode.of(object.getName());
    }

    @Override
    public User fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return um.findUser(((StringNode)node).getValue()).asPlayer();

            // TODO wait for UserStorage impl return userStorage.get(((StringNode)node).getValue()).orNull();
        }
        throw ConversionException.of(this, node, "Node is not a StringNode!");
    }
}
