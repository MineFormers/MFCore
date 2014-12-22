package de.mineformers.core.client.renderer.shape

import de.mineformers.core.client.renderer.RenderParams
import de.mineformers.core.client.util.Color
import org.lwjgl.opengl.GL11

/**
 * Face
 *
 * @author PaleoCrafter
 */
class Face(val vertices: List[Vertex], params0: RenderParams) {
  val params = if(params0 != null) params0.drawingMode(vertices.size match {
    case 1 => GL11.GL_POINTS
    case 2 => GL11.GL_LINES
    case 3 => GL11.GL_TRIANGLES
    case 4 => GL11.GL_QUADS
    case _ => params0.mode
  }) else null

  def this(vertices: Vertex*) = this(vertices.toList, null)

  def color_=(c: Color) = new Face(vertices map { v => Vertex(v.pos, v.uv, c)}, params)
}
