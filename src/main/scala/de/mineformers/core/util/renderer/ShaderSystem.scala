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
package de.mineformers.core.util.renderer

import org.lwjgl.opengl.GL11.{GL_TRUE, glGetInteger}
import org.lwjgl.opengl.GL20._

import scala.collection.mutable

/**
 * ShaderSystem
 *
 * @author PaleoCrafter
 */
class ShaderSystem(initShaders: (String, Int)*) {
  private val program: Int = glCreateProgram()
  for ((shader, typ) <- initShaders)
    addShader(shader, typ)

  def init(): Unit = {
    glLinkProgram(program)
    glValidateProgram(program)
    initialized = true
    active = glGetProgrami(program, GL_LINK_STATUS) == GL_TRUE
  }

  def activate(): Unit = {
    if (!initialized) init()
    if (active) {
      lastProgram = glGetInteger(GL_CURRENT_PROGRAM)
      glUseProgram(program)
    }
  }

  def deactivate(): Unit = {
    if (!initialized) init()
    if (active) glUseProgram(lastProgram)
  }

  def addShader(source: String, `type`: Int) {
    if (!initialized) {
      try {
        glAttachShader(program, createShader(source, `type`))
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }
  }

  private def createShader(source: String, shaderType: Int): Int = {
    var shader: Int = 0
    try {
      shader = glCreateShader(shaderType)
      if (shader == 0) return 0
      glShaderSource(shader, source)
      glCompileShader(shader)
      shader
    } catch {
      case exc: Exception =>
        glDeleteShader(shader)
        throw exc
    }
  }

  def setUniform1f(uniform: String, value: Float): Unit = {
    if (active) glUniform1f(getUniformLocation(uniform), value)
  }

  def setUniform1i(uniform: String, value: Int): Unit = {
    if (active) glUniform1i(getUniformLocation(uniform), value)
  }

  def setUniform2f(uniform: String, v1: Float, v2: Float): Unit = {
    if (active) glUniform2f(getUniformLocation(uniform), v1, v2)
  }

  def getUniformLocation(uniform: String): Int = {
    if (!varLocations.contains(uniform)) {
      varLocations.put(uniform, glGetUniformLocation(program, uniform))
    }
    varLocations(uniform)
  }

  private val varLocations: mutable.HashMap[String, Int] = mutable.HashMap[String, Int]()
  private var lastProgram: Int = 0
  private var initialized: Boolean = false
  private var active: Boolean = false
}