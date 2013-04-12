package de.cubeisland.cubeengine.travel.interactions;

import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.command.ArgBounds;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.result.AsyncResult;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.travel.Travel;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;
import de.cubeisland.cubeengine.travel.storage.TeleportPoint;
import de.cubeisland.cubeengine.travel.storage.Warp;

public class WarpCommand extends ContainerCommand
{
    private final Travel module;
    private final TelePointManager telePointManager;

    public WarpCommand(Travel module)
    {
        super(module, "warp", "Teleport to a warp");
        this.module = module;
        this.telePointManager = module.getTelepointManager();

        this.getContextFactory().setArgBounds(new ArgBounds(0, 1));
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.isSender(User.class) && context.getArgCount() > 0)
        {
            User sender = (User)context.getSender();
            Warp warp = telePointManager.getWarp(sender, context.getString(0).toLowerCase());
            if (warp == null)
            {
                context.sendTranslated("&4You don't have access to any warp with that name");
                return null;
            }

            sender.teleport(warp.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            context.sendTranslated("&6You have been teleported to the warp &9%s", context.getString(0));
        }
        else
        {
            return super.run(context);
        }
        return null;
    }


    @Alias(names = {
        "createwarp", "mkwarp", "makewarp"
    })
    @Command(names = {
        "create", "make"
    }, flags = {
        @Flag(name = "priv", longName = "private")
    }, permDefault = PermDefault.OP, desc = "Create a warp", min = 1, max = 1)
    public void createWarp(ParameterizedContext context)
    {
        if (this.telePointManager.getNumberOfWarps() == this.module.getConfig().maxwarps)
        {
            context.sendTranslated("The server have reached it's maximum number of warps!");
            context.sendTranslated("Some warps have to be delete for new ones to be made");
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            String name = context.getString(0);
            if (telePointManager.hasWarp(name) && !context.hasFlag("priv"))
            {
                context
                    .sendTranslated("A public warp by that name already exist! maybe you want to include the -private flag?");
                return;
            }
            if (name.contains(":") || name.length() >= 32)
            {
                context
                    .sendTranslated("&4Warps may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
                return;
            }
            Location loc = sender.getLocation();
            Warp warp = telePointManager.createWarp(loc, name, sender, (context
                                                                            .hasFlag("priv") ? TeleportPoint.Visibility.PRIVATE : TeleportPoint.Visibility.PUBLIC));
            context.sendTranslated("Your warp have been created");
            return;
        }
        context.sendTranslated("You have to be in the world to set a warp");
    }

    @Alias(names = {
        "removewarp", "deletewarp", "delwarp", "remwarp"
    })
    @Command(names = {
        "remove", "delete"
    }, permDefault = PermDefault.OP, desc = "Remove a warp", min = 1, max = 1)
    public void removeWarp(CommandContext context)
    {
        Warp warp;
        if (context.getSender() instanceof User)
        {
            warp = telePointManager.getWarp((User)context.getSender(), context.getString(0));
        }
        else
        {
            warp = telePointManager.getWarp(context.getString(0));
        }
        if (warp == null)
        {
            context.sendTranslated("The warp could not be found");
            return;
        }
        telePointManager.deleteWarp(warp);
        context.sendTranslated("The warp is now deleted");
    }

    @Command(permDefault = PermDefault.OP, desc = "Rename a warp", min = 2, max = 2)
    public void rename(CommandContext context)
    {
        String name = context.getString(1);
        Warp warp;
        if (context.getSender() instanceof User)
        {
            warp = telePointManager.getWarp((User)context.getSender(), context.getString(0));
        }
        else
        {
            warp = telePointManager.getWarp(context.getString(0));
        }
        if (warp == null)
        {
            context.sendTranslated("The warp could not be found");
            return;
        }

        if (name.contains(":") || name.length() >= 32)
        {
            context
                .sendTranslated("&4Warps may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
            return;
        }

        telePointManager.renameWarp(warp, name);
        context.sendTranslated("The warps name is now changed");
    }

    @Command(permDefault = PermDefault.OP, desc = "Move a warp", min = 1, max = 2)
    public void move(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (!(sender instanceof User))
        {
            return;
        }
        User user = (User)sender;

        Warp warp = telePointManager.getWarp(user, context.getString(0));
        if (warp == null)
        {
            user.sendTranslated("That warp could not be found!");
            return;
        }
        if (!warp.isOwner(user))
        {
            user.sendTranslated("You are not allowed to edit that warp!");
            return;
        }
        warp.setLocation(user.getLocation());
        warp.update();
        user.sendTranslated("The warp is now moved to your current location");
    }

    @Command(permDefault = PermDefault.TRUE, desc = "Search for a warp", min = 1, max = 2)
    public CommandResult search(CommandContext context)
    {
        String search = context.getString(0);
        Warp first;
        if (context.getSender() instanceof User)
        {
            first = telePointManager.getWarp((User)context.getSender(), search);
        }
        else
        {
            first = telePointManager.getWarp(search);
        }
        if (first != null)
        {
            context.sendTranslated("Found a direct match: %s owned by %s", first.getName(), first.getOwner()
                                                                                                 .getDisplayName());
            return null;
        }

        return new AsyncResult()
        {
            TreeMap<String, Integer> results;

            @Override
            public void asyncMain(CommandContext context)
            {
                results = telePointManager.searchWarp(context.getString(0), context.getSender());
            }

            @Override
            public void onFinish(CommandContext context)
            {
                context.sendTranslated("Here is the top %d results:", context.getArg(1, Integer.class, 5));
                int position = 1;
                for (String warp : results.keySet())
                {
                    context.sendMessage(position++ + ". " + warp);
                    if (position == context.getArg(1, Integer.class, 5))
                    {
                        break;
                    }
                }
            }
        };
    }

    @Command(permDefault = PermDefault.TRUE, desc = "List all available warps", flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "private"),
        @Flag(name = "o", longName = "owned"),
        @Flag(name = "i", longName = "invited")
    }, usage = "<user> <-PUBlic> <-PRIVate> <-Owned> <-Invited>", min = 0, max = 1)
    public void list(ParameterizedContext context)
    {
        int mask = context.getFlagCount() < 1 ? telePointManager.ALL : 0;
        if (context.hasFlag("pub"))
        {
            mask |= telePointManager.PUBLIC;
        }
        if (context.hasFlag("priv"))
        {
            mask |= telePointManager.PRIVATE;
        }
        if (context.hasFlag("o"))
        {
            mask |= telePointManager.OWNED;
        }
        if (context.hasFlag("i"))
        {
            mask |= telePointManager.INVITED;
        }

        Set<Warp> warps;
        if (context.getArgCount() == 1)
        {
            User user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated("%s is not a user on this server");
                return;
            }
            warps = telePointManager.listWarps(context.getUser(0), mask);
        }
        else
        {
            warps = telePointManager.listWarps(mask);
        }

        if (warps.isEmpty())
        {
            context.sendTranslated("The query returned no warps!");

        }
        else
        {
            context.sendTranslated("Here are the warps:");
            for (Warp warp : warps)
            {
                context.sendMessage(warp.getOwner().getDisplayName() + ":" + warp.getName());
            }
        }
    }
}
