package de.cubeisland.cubeengine.log.storage;

import org.bukkit.Material;
import org.bukkit.block.BlockState;

public class BlockData
{
    public Material mat;
    public byte data;

    public BlockData(Material mat, byte data)
    {
        this.mat = mat;
        this.data = data;
    }

    public BlockState applyTo(BlockState state)
    {
        state.setType(mat);
        state.setRawData(data);
        return state;
    }
}
