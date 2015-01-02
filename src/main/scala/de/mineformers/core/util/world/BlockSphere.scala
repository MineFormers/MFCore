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
package de.mineformers.core.util.world

import de.mineformers.core.util.math.Vector3
import net.minecraft.world.World

import scala.language.postfixOps
import scala.util.control.Breaks

/**
 * BlockSphere
 *
 * @author PaleoCrafter
 */
object BlockSphere {
  def simple(center: BlockPos, radius: Double)(operation: (BlockPos) => Unit) = {
    val radiusX = radius + 0.5D
    val radiusY = radius + 0.5D
    val radiusZ = radius + 0.5D
    val invRadiusX = 1 / radiusX
    val invRadiusY = 1 / radiusY
    val invRadiusZ = 1 / radiusZ

    val ceilRadiusX = math.ceil(radiusX).toInt
    val ceilRadiusY = math.ceil(radiusY).toInt
    val ceilRadiusZ = math.ceil(radiusZ).toInt
    var nextXn = 0D
    val xBreaks = new Breaks
    val yBreaks = new Breaks
    val zBreaks = new Breaks
    xBreaks breakable {
      for (x <- 0 to ceilRadiusX) {
        val xn = nextXn
        nextXn = (x + 1) * invRadiusX
        var nextYn = 0D
        yBreaks.breakable {
          for (y <- 0 to ceilRadiusY) {
            val yn = nextYn
            nextYn = (y + 1) * invRadiusY
            var nextZn = 0D
            zBreaks.breakable {
              for (z <- 0 to ceilRadiusZ) {
                val zn = nextZn
                nextZn = (z + 1) * invRadiusZ

                val distanceSq = Vector3(xn, yn, zn).magSq
                if (distanceSq > 1) {
                  if (z == 0) {
                    if (y == 0) {
                      xBreaks.break()
                    }
                    yBreaks.break()
                  }
                  zBreaks.break()
                }

                val xInt = x.toInt
                val yInt = y.toInt
                val zInt = z.toInt

                Seq(BlockPos(xInt, yInt, zInt),
                  BlockPos(-xInt, yInt, zInt),
                  BlockPos(xInt, -yInt, zInt),
                  BlockPos(xInt, yInt, -zInt),
                  BlockPos(-xInt, -yInt, zInt),
                  BlockPos(xInt, -yInt, -zInt),
                  BlockPos(-xInt, yInt, -zInt),
                  -BlockPos(xInt, yInt, zInt)) map {
                  _ + center
                } foreach {
                  p =>
                    if (p.y > 0)
                      operation(p)
                }
              }
            }
          }
        }
      }
    }
  }
}

case class BlockSphere(center: BlockPos, radius: Double, var operation: (BlockPos) => Unit = pos => ()) {
  val blocks = {
    var acc = Seq.empty[BlockPos]
    val radiusX = radius + 0.5D
    val radiusY = radius + 0.5D
    val radiusZ = radius + 0.5D
    val invRadiusX = 1 / radiusX
    val invRadiusY = 1 / radiusY
    val invRadiusZ = 1 / radiusZ

    val ceilRadiusX = math.ceil(radiusX).toInt
    val ceilRadiusY = math.ceil(radiusY).toInt
    val ceilRadiusZ = math.ceil(radiusZ).toInt
    var nextXn = 0D
    val xBreaks = new Breaks
    val yBreaks = new Breaks
    val zBreaks = new Breaks
    xBreaks breakable {
      for (x <- 0 to ceilRadiusX) {
        val xn = nextXn
        nextXn = (x + 1) * invRadiusX
        var nextYn = 0D
        yBreaks.breakable {
          for (y <- 0 to ceilRadiusY) {
            val yn = nextYn
            nextYn = (y + 1) * invRadiusY
            var nextZn = 0D
            zBreaks.breakable {
              for (z <- 0 to ceilRadiusZ) {
                val zn = nextZn
                nextZn = (z + 1) * invRadiusZ

                val distanceSq = Vector3(xn, yn, zn).magSq
                if (distanceSq > 1) {
                  if (z == 0) {
                    if (y == 0) {
                      xBreaks.break()
                    }
                    yBreaks.break()
                  }
                  zBreaks.break()
                }

                val xInt = x.toInt
                val yInt = y.toInt
                val zInt = z.toInt

                acc ++= Seq(BlockPos(xInt, yInt, zInt),
                  BlockPos(-xInt, yInt, zInt),
                  BlockPos(xInt, -yInt, zInt),
                  BlockPos(xInt, yInt, -zInt),
                  BlockPos(-xInt, -yInt, zInt),
                  BlockPos(xInt, -yInt, -zInt),
                  BlockPos(-xInt, yInt, -zInt),
                  -BlockPos(xInt, yInt, zInt)) map {
                  _ + center
                }
              }
            }
          }
        }
      }
    }
    acc.distinct filter {
      _.y >= 0
    }
  }.toBuffer

  def isAir(world: World): Boolean = {
    blocks forall (world.isAirBlock(_))
  }

  def noAir(world: World): BlockSphere = {
    blocks foreach {
      p => if (p != null && world.isAirBlock(p))
        blocks -= p
    }
    this
  }

  def intersects(sphere: BlockSphere): Boolean = {
    !sphere.blocks.forall(!blocks.contains(_))
  }

  def contains(pos: BlockPos) = blocks.contains(pos)

  def walkAll(callback: => Unit = ()): Unit = {
    blocks foreach operation
  }

  def walk(): Boolean = {
    if (i < blocks.length) {
      val pos = blocks(i)
      if (pos != null) {
        i += 1
        operation(pos)
        true
      } else false
    } else
      false
  }

  def reset(): Unit = i = 0

  private var i = 0
}