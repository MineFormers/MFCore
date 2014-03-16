package de.mineformers.core.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public class RenderBlockEvent extends Event {

    public final IBlockAccess blockAccess;
    public final int x, y, z;
    public final Block block;
    public final int renderType;
    public final RenderBlocks renderer;

    public RenderBlockEvent(IBlockAccess blockAccess, int x, int y, int z, Block block, int renderType, RenderBlocks renderer) {
        this.blockAccess = blockAccess;
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.renderType = renderType;
        this.renderer = renderer;
    }

    @Cancelable
    public static class Pre extends RenderBlockEvent {
        public Pre(IBlockAccess blockAccess, int x, int y, int z, Block block, int renderType, RenderBlocks renderer) {
            super(blockAccess, x, y, z, block, renderType, renderer);
        }
    }

    public static class Post extends RenderBlockEvent {
        public Post(IBlockAccess blockAccess, int x, int y, int z, Block block, int renderType, RenderBlocks renderer) {
            super(blockAccess, x, y, z, block, renderType, renderer);
        }
    }

}
