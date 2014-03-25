/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MineFormers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package de.mineformers.core.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * RenderBlockEvent
 * Called whenever a standard block is rendered (from RenderBlocks)
 *
 * @author PaleoCrafter
 */
public class RenderBlockEvent extends Event {

    public final IBlockAccess blockAccess;
    public final int x, y, z;
    public final Block block;
    public final int renderType;
    public final int renderPass;
    public final RenderBlocks renderer;

    public RenderBlockEvent(IBlockAccess blockAccess, int x, int y, int z, Block block, int renderType, int renderPass, RenderBlocks renderer) {
        this.blockAccess = blockAccess;
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.renderType = renderType;
        this.renderPass = renderPass;
        this.renderer = renderer;
    }

    /**
     * The event triggered BEFORE the actual block is rendered.
     * Can be cancelled to prevent the vanilla block from rendering
     */
    @Cancelable
    public static class Pre extends RenderBlockEvent {
        public Pre(IBlockAccess blockAccess, int x, int y, int z, Block block, int renderType, int renderPass, RenderBlocks renderer) {
            super(blockAccess, x, y, z, block, renderType, renderPass, renderer);
        }
    }

    /**
     * The event triggered AFTER the actual block is rendered.
     * Will also be called if the Pre event was cancelled.
     */
    public static class Post extends RenderBlockEvent {
        public Post(IBlockAccess blockAccess, int x, int y, int z, Block block, int renderType, int renderPass, RenderBlocks renderer) {
            super(blockAccess, x, y, z, block, renderType, renderPass, renderer);
        }
    }

}
