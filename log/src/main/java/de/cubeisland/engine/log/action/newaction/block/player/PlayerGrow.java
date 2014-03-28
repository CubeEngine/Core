package de.cubeisland.engine.log.action.newaction.block.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockListener;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.AIR;

/**
 * Represents a player letting a tree or mushroom grow using bonemeal
 */
public class PlayerGrow extends PlayerBlockActionType<BlockListener>
{
    // return "player-grow";
    // return this.lm.getConfig(world).block.grow.PLAYER_GROW_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerGrow
            && this.player.equals(((PlayerGrow)action).player)
            && ((PlayerGrow)action).oldBlock == this.oldBlock
            && ((PlayerGrow)action).newBlock == this.newBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.oldBlock == AIR)
        {
            return user.getTranslationN(POSITIVE, count,
                                        "{user} let grow {name#block}",
                                        "{user} let grow {2:amount}x {name#block}",
                                        this.player.name, this.newBlock.name(), count);
        }
        return user.getTranslationN(POSITIVE, count,
                                    "{user} let grow {name#block} into {name#block}",
                                    "{user} let grow {3:amount}x {name#block} into {name#block}",
                                    this.player.name, this.newBlock.name(), this.oldBlock.name(), count);
    }
}
