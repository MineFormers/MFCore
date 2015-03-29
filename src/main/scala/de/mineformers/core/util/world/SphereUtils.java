/*
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
 */

package de.mineformers.core.util.world;

import de.mineformers.core.util.math.Vector3;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * SphereUtils
 *
 * @author PaleoCrafter
 */
public class SphereUtils
{
    public static void set(World world, Block block, BlockPos center, double radius)
    {
        radius += 0.5;

        final double invRadiusX = 1 / radius;
        final double invRadiusY = 1 / radius;
        final double invRadiusZ = 1 / radius;
        final int ceilRadiusX = (int) Math.ceil(radius);
        final int ceilRadiusY = (int) Math.ceil(radius);
        final int ceilRadiusZ = (int) Math.ceil(radius);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= ceilRadiusX; ++x)
        {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= ceilRadiusY; ++y)
            {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ:
                for (int z = 0; z <= ceilRadiusZ; ++z)
                {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = Vector3.apply(xn, yn, zn).magSq();
                    if (distanceSq > 1)
                    {
                        if (z == 0)
                        {
                            if (y == 0)
                            {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    setBlock(world, center.$plus(x, y, z), block);
                    setBlock(world, center.$plus(-x, y, z), block);
                    setBlock(world, center.$plus(x, -y, z), block);
                    setBlock(world, center.$plus(x, y, -z), block);
                    setBlock(world, center.$plus(-x, -y, z), block);
                    setBlock(world, center.$plus(x, -y, -z), block);
                    setBlock(world, center.$plus(-x, y, -z), block);
                    setBlock(world, center.$plus(-x, -y, -z), block);
                }
            }
        }
    }

    public static void destroy(World world, BlockPos center, double radius)
    {
        radius += 0.5;

        final double invRadiusX = 1 / radius;
        final double invRadiusY = 1 / radius;
        final double invRadiusZ = 1 / radius;
        final int ceilRadiusX = (int) Math.ceil(radius);
        final int ceilRadiusY = (int) Math.ceil(radius);
        final int ceilRadiusZ = (int) Math.ceil(radius);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= ceilRadiusX; ++x)
        {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= ceilRadiusY; ++y)
            {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ:
                for (int z = 0; z <= ceilRadiusZ; ++z)
                {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = Vector3.apply(xn, yn, zn).magSq();
                    if (distanceSq > 1)
                    {
                        if (z == 0)
                        {
                            if (y == 0)
                            {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    destroyBlock(world, center.$plus(x, y, z));
                    destroyBlock(world, center.$plus(-x, y, z));
                    destroyBlock(world, center.$plus(x, -y, z));
                    destroyBlock(world, center.$plus(x, y, -z));
                    destroyBlock(world, center.$plus(-x, -y, z));
                    destroyBlock(world, center.$plus(x, -y, -z));
                    destroyBlock(world, center.$plus(-x, y, -z));
                    destroyBlock(world, center.$plus(-x, -y, -z));
                }
            }
        }
    }

    private static void destroyBlock(World world, BlockPos pos)
    {
        if (world.getBlockState(BlockPos.custom2vanilla(pos)).getBlock().getBlockHardness(world, BlockPos.custom2vanilla(pos)) >= 0)
            world.setBlockState(BlockPos.custom2vanilla(pos), Blocks.air.getDefaultState(), 2);
    }

    private static void setBlock(World world, BlockPos pos, Block block)
    {
        world.setBlockState(BlockPos.custom2vanilla(pos), block.getDefaultState(), 2);
    }
}
