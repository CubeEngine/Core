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
package de.cubeisland.engine.log.action.newaction.block;

import java.util.Map;import java.util.concurrent.ConcurrentHashMap;import org.bukkit.Location;import org.bukkit.Material;import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.PistonExtensionMaterial;

import com.fasterxml.jackson.databind.node.ObjectNode;import com.sun.xml.internal.bind.v2.TODO;
import de.cubeisland.engine.core.util.Pair;import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.logaction.block.*;import de.cubeisland.engine.log.action.logaction.block.BlockActionType;import de.cubeisland.engine.log.action.logaction.block.BlockActionType.BlockData;import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerGrow;

import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.DOWN;

/**
 * A Listener for EntityBlock Actions
 * <p>Events:
 * {@link BlockFadeEvent}
 * {@link BlockBurnEvent}
 * {@link BlockFormEvent}
 * {@link BlockSpreadEvent}
 * {@link LeavesDecayEvent}
 * {@link StructureGrowEvent}
 * {@link BlockPistonExtendEvent}
 * {@link BlockPistonRetractEvent}
 * <p>Actions:
 * {@link BlockBurn}
 * {@link BlockFade}
 * {@link BlockForm}
 * {@link BlockSpread}
 * {@link LeafDecay}
 * {@link NaturalGrow}
 * {@link BlockShift}
 * <p>External Actions:
 * {@link PlayerGrow}
 */
public class BlockListener extends LogListener
{
    private static final PistonExtensionMaterial PISTON_HEAD = new PistonExtensionMaterial(PISTON_EXTENSION);
    private final Map<Location, PlayerBlockActionType> plannedFall = new ConcurrentHashMap<>();

    public BlockListener(Log module)
    {
        super(module);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        BlockBurn action = this.newAction(BlockBurn.class, event.getBlock().getWorld());
        if (action != null)
        {
            BlockState state = adjustBlockForDoubleBlocks(event.getBlock().getState());
            action.setOldBlock(state);
            action.setLocation(state.getLocation());
            action.setNewBlock(AIR);
            this.logAction(action);
        }
        // TODO
        this.logAttachedBlocks(event.getBlock().getState(), null);
        this.logFallingBlocks(event.getBlock().getState(), null);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event)
    {
        BlockFade action = this.newAction(BlockFade.class, event.getBlock().getWorld());
        if (action != null)
        {
            // TODO if (!this.lm.getConfig(event.getBlock().getWorld()).block.fade.ignore.contains(event.getBlock().getType()))
            action.setLocation(event.getBlock().getLocation());
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(AIR);
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        if (event instanceof EntityBlockFormEvent)
        {
            return;
        }
        // TODO block.form.BLOCK_FORM_ignore
        BlockForm action = this.newAction(BlockForm.class, event.getBlock().getWorld());
        if (action != null)
        {
            action.setLocation(event.getBlock().getLocation());
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(event.getNewState());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event)
    {
        if (!(event.getNewState().getType() == FIRE))
        {
            BlockSpread action = this.newAction(BlockSpread.class, event.getBlock().getWorld());
            if (action != null)
            {
                action.setLocation(event.getBlock().getLocation());
                action.setOldBlock(event.getBlock().getState());
                action.setNewBlock(event.getNewState());
                this.logAction(action);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        LeafDecay action = this.newAction(LeafDecay.class, event.getBlock().getWorld());
        if (action != null)
        {
            action.setLocation(event.getBlock().getLocation());
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(AIR);
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        if (event.getPlayer() == null)
        {
            if (this.isActive(NaturalGrow.class, event.getWorld()))
            {
                for (BlockState newState : event.getBlocks())
                {
                    BlockState oldState = newState.getBlock().getState();  // TODO is this working?
                    if (oldState.getData().equals(newState.getData()))
                    {
                        continue;
                    }
                    NaturalGrow action = this.newAction(NaturalGrow.class);
                    action.setNewBlock(newState);
                    action.setOldBlock(oldState);
                    action.setLocation(newState.getLocation());
                }
            }
        }
        else
        {
            if (this.isActive(PlayerGrow.class, event.getWorld()))
            {
                for (BlockState newState : event.getBlocks())
                {
                    BlockState oldState = newState.getBlock().getState();  // TODO is this working?
                    if (oldState.getData().equals(newState.getData()))
                    {
                        continue;
                    }
                    PlayerGrow action = this.newAction(PlayerGrow.class);
                    action.setNewBlock(newState);
                    action.setOldBlock(oldState);
                    action.setLocation(newState.getLocation());
                    action.setPlayer(event.getPlayer());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent event)
    {
        if (this.isActive(BlockShift.class, event.getBlock().getWorld()))
        {
            boolean first = true;
            if (event.getBlocks().isEmpty())
            {
                PistonExtensionMaterial pistonHead = PISTON_HEAD.clone();
                pistonHead.setSticky(event.isSticky());
                pistonHead.setFacingDirection(event.getDirection());

                Block firstBlock = event.getBlock().getRelative(event.getDirection());
                BlockState newState = firstBlock.getState();
                newState.setData(pistonHead);

                BlockShift action = this.newAction(BlockShift.class);
                action.setOldBlock(firstBlock.getState());
                action.setNewBlock(newState);
                action.setLocation(firstBlock.getLocation());
                action.setPush();
                this.logAction(action);
                return;
            }
            for (Block block : event.getBlocks())
            {
                BlockState state = block.getState();
                if (first)
                {
                    first = false;
                    PistonExtensionMaterial pistonHead = PISTON_HEAD.clone();
                    pistonHead.setSticky(event.isSticky());
                    pistonHead.setFacingDirection(event.getDirection());

                    BlockState newState = block.getState();
                    newState.setData(pistonHead);

                    BlockShift action = this.newAction(BlockShift.class);
                    action.setOldBlock(state);
                    action.setNewBlock(newState);
                    action.setLocation(block.getLocation());
                    action.setPush();
                    this.logAction(action);
                }
                if (block.getType() == AIR)
                {
                    continue;
                }

                BlockState newState = block.getRelative(event.getDirection()).getState();
                newState.setData(state.getData());

                BlockShift action = this.newAction(BlockShift.class);
                action.setOldBlock(newState.getBlock().getState());
                action.setNewBlock(newState);
                action.setLocation(newState.getLocation());
                action.setPush();
                this.logAction(action);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event)
    {
        if (this.isActive(BlockShift.class, event.getBlock().getWorld()))
        {
            BlockState retractedBlock = event.getRetractLocation().getBlock().getState();
            BlockState pistonHead = event.getBlock().getRelative(event.getDirection()).getState();

            BlockShift action = this.newAction(BlockShift.class);
            action.setOldBlock(pistonHead);
            BlockState newPistonHead = pistonHead.getBlock().getState();
            if (retractedBlock.getType().isSolid())
            {
                newPistonHead.setData(retractedBlock.getData());
            }
            else
            {
                newPistonHead.setType(AIR);
            }
            action.setNewBlock(newPistonHead);
            action.setLocation(pistonHead.getLocation());
            action.setRetract();
            this.logAction(action);

            if (event.isSticky() && retractedBlock.getType().isSolid()) // TODO missing retractable blocks?
            {
                action = this.newAction(BlockShift.class);
                action.setOldBlock(retractedBlock);
                action.setNewBlock(AIR);
                action.setLocation(retractedBlock.getLocation());
                action.setRetract();
                this.logAction(action);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent event)
    {
        if (!this.isActive(BlockFall.class, event.getBlock().getWorld())) return;
        BlockState state = event.getBlock().getState();
        if (state.getType().hasGravity() || state.getType() == DRAGON_EGG)
        { // falling blocks
            if (event.getBlock().getRelative(DOWN).getType() == AIR)
            {
                Location loc = state.getLocation();

                BlockFall action = this.newAction(BlockFall.class, state.getWorld());
                if (action != null)
                {
                    PlayerBlockActionType cause = this.plannedFall.remove(loc);
                    action.cause = this.reference(cause);
                    action.setOldBlock(state);
                    action.setNewBlock(AIR);
                    action.setLocation(state.getLocation());
                    this.logAction(action);

                    Block onTop = state.getBlock().getRelative(BlockFace.UP);
                    if (onTop.getType().hasGravity() || onTop.getType() == DRAGON_EGG)
                    {
                        this.preplanBlockFall(new BlockPreFallEvent(onTop.getLocation(), cause));
                    }
                }
            }
        }
    }

    @EventHandler
    public void preplanBlockFall(final BlockPreFallEvent event)
    {
        plannedFall.put(event.getLocation(), event.getAction());
        this.module.getCore().getTaskManager().runTaskDelayed(module, new Runnable()
        {
            @Override
            public void run()
            {
                plannedFall.remove(event.getLocation());
            }
        }, 3);
    }
}
