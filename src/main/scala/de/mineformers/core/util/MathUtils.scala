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

package de.mineformers.core.util

/**
 * MathUtils
 *
 * @author PaleoCrafter
 */
object MathUtils {
  def easeInOut(time: Double, start: Double, change: Double, duration: Double): Double = {
    var t = time / (duration / 2)
    if (t < 1) return change / 2 * t * t + start
    t -= 1
    -change / 2 * (t * (t - 2) - 1) + start
  }

  def pulsate(time: Double, start: Double, change: Double, duration: Double): Double = {
    if (time < duration / 2)
      easeInOut(time, start, change, duration / 2)
    else
      easeInOut(time - duration / 2, start + change, -change, duration / 2)
  }

  def scale(valueIn: Double, baseMin: Double, baseMax: Double, limitMin: Double, limitMax: Double): Double =
    ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin

  def wrapAround(value: Int, max: Int, min: Int = 0) = if(value > max) min else value
}
