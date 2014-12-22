package de.mineformers.core.client.renderer.shape

import de.mineformers.core.client.renderer.RenderParams
import de.mineformers.core.util.world.Vector3
import org.lwjgl.opengl.GL11._

/**
 * Circle
 *
 * @author PaleoCrafter
 */
class Circle(radius: Double, accuracy: Int, filled: Boolean = true) extends Face((0 until accuracy).toList map {
  i =>
    val theta = i * 2 * math.Pi / accuracy.toFloat
    val x = radius * math.cos(theta)
    val y = radius * math.sin(theta)
    Vertex(Vector3(x, y, 0))
} reverse, RenderParams.start().drawingMode(if(filled) GL_TRIANGLE_FAN else GL_LINE_LOOP).build())