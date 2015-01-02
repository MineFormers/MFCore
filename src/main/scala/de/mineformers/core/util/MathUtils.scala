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

import de.mineformers.core.util.math.{Matrix4, Vector3}

import scala.math._

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
    abs(start + change * sin(2 * Pi * time * (1 / (duration * 2))))
  }

  def scale(valueIn: Double, baseMin: Double, baseMax: Double, limitMin: Double, limitMax: Double): Double =
    ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin

  def wrapAround(value: Int, max: Int, min: Int = 0) = if (value > max) min else value

  def intToByteArray(value: Int): Array[Byte] =
    Array((value >>> 24).toByte, (value >>> 16).toByte, (value >>> 8).toByte, value.toByte)

  def createProjectionMatrixAsPerspective(fovDegrees: Double, near: Double, far: Double, viewportWidth: Int, viewportHeight: Int) = {
    // for impl details see gluPerspective doco in OpenGL reference manual
    val aspect = viewportWidth.toDouble / viewportHeight.toDouble

    val theta = Math.toRadians(fovDegrees) / 2d
    val f = Math.cos(theta) / Math.sin(theta)

    val a = (far + near) / (near - far)
    val b = (2d * far * near) / (near - far)

    new Matrix4(f / aspect, 0, 0, 0, 0, f, 0, 0, 0, 0, a, b, 0, 0, -1, 0)
  }

  def createMatrixAsLookAt(eye: Vector3, lookAt: Vector3, up: Vector3) = {
    val forwardVec = (lookAt - eye).normalize
    val sideVec = forwardVec.crossProduct(up).normalize
    val upVed = sideVec.crossProduct(forwardVec).normalize

    val mat = new Matrix4(
      sideVec.x, sideVec.y, sideVec.z, 0,
      upVed.x, upVed.y, upVed.z, 0,
      -forwardVec.x, -forwardVec.y, -forwardVec.z, 0,
      0, 0, 0, 1)

    mat.withTranslation(mat.transformNormal(-eye))
  }
}
