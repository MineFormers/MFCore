package de.mineformers.core.client.renderer

import de.mineformers.core.client.renderer.RenderParams.RPBuilding
import de.mineformers.core.client.renderer.shape.{Face, Shape, Vertex}
import de.mineformers.core.client.util.Color
import de.mineformers.core.util.world.Vector3
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import org.lwjgl.opengl.GL11._

import scala.collection.mutable.ListBuffer

/**
 * Renderer
 *
 * @author PaleoCrafter
 */
class Renderer {
  val context = new RenderContext

  def renderShapes(shapes: Traversable[Shape], params: RenderParams): Unit = {
    glPushMatrix()
    if (params != null)
      params(context)
    for (s <- shapes) {
      renderFaces(s.faces, s.params)
    }
    if (params != null)
      params.unapply()
    glPopMatrix()
  }

  def renderFaces(faces: Traversable[Face], params: RenderParams): Unit = {
    glPushMatrix()
    if (params != null)
      params(context)
    for (f <- faces) {
      render(f.vertices, f.params)
    }
    if (params != null)
      params.unapply()
    glPopMatrix()
  }

  def render(vertices: Traversable[Vertex], params: RenderParams): Unit = {
    glPushMatrix()
    if (params != null)
      params(context)
    val tessellator: Tessellator = Tessellator.instance
    tessellator.startDrawing(params.mode)
    for (v <- vertices) {
      v.addToTessellator(tessellator)
    }
    tessellator.draw()
    if (params != null)
      params.unapply()
    glPopMatrix()
  }
}

object RenderContext {

  case class Block(world: World, x: Int, y: Int, z: Int) {
    def tile[T <: TileEntity] = world.getTileEntity(x, y, z).asInstanceOf[T]
  }

  case class Item(stack: ItemStack)

}

class RenderContext {
  var pos: Vector3 = _
  var block: RenderContext.Block = _
  var item: RenderContext.Item = _
}

object RenderParams {
  def start() = new Builder

  private[RenderParams] class Builder extends RPBuilding[Builder] {
    private val modifiers = ListBuffer.empty[Modifier]
    private val transformations = ListBuffer.empty[Transformation]
    private var mode = 0

    override def drawingMode(mode: Int) = {
      this.mode = mode
      this
    }

    override def customModifier(mod: Modifier) = {
      modifiers += mod
      this
    }

    override def customTransformation(trans: Transformation) = {
      transformations += trans
      this
    }

    def build() = RenderParams(mode, modifiers.toList, transformations.toList)
  }

  private[RenderParams] trait RPBuilding[R] {
    def drawingMode(mode: Int): R

    def blend(src: Int = GL_SRC_ALPHA, dest: Int = GL_ONE_MINUS_SRC_ALPHA) = customModifier(new Blending(src, dest))

    def color(c: RenderContext => Color) = customModifier(new Coloring(c))

    def color(c: => Color) = customModifier(new Coloring(c))

    def center(x: RenderContext => Double, y: RenderContext => Double, z: RenderContext => Double) = customTransformation(Translation.center(x, y, z))

    def center(x: => Double = 0D, y: => Double = 0D, z: => Double = 0D) = customTransformation(Translation.center(x, y, z))

    def translate(x: RenderContext => Double, y: RenderContext => Double, z: RenderContext => Double) = customTransformation(new Translation(x, y, z))

    def translate(x: => Double, y: => Double, z: => Double) = customTransformation(new Translation(x, y, z))

    def rotateX(angle: RenderContext => Double) = customTransformation(Rotation.aroundX(angle))

    def rotateY(angle: RenderContext => Double) = customTransformation(Rotation.aroundY(angle))

    def rotateZ(angle: RenderContext => Double) = customTransformation(Rotation.aroundZ(angle))

    def rotateX(angle: => Double) = customTransformation(Rotation.aroundX(angle))

    def rotateY(angle: => Double) = customTransformation(Rotation.aroundY(angle))

    def rotateZ(angle: => Double) = customTransformation(Rotation.aroundZ(angle))

    def rotate(angle: RenderContext => Double, x: RenderContext => Double, y: RenderContext => Double, z: RenderContext => Double) = customTransformation(new Rotation(angle, x, y, z))

    def rotate(angle: => Double, x: => Double, y: => Double, z: => Double) = customTransformation(new Rotation(angle, x, y, z))

    def scale(x: RenderContext => Double, y: RenderContext => Double, z: RenderContext => Double) = customTransformation(new Scaling(x, y, z))

    def scale(x: => Double, y: => Double, z: => Double) = customTransformation(new Scaling(x, y, z))

    def scaleAll(sc: RenderContext => Double) = scale(sc, sc, sc)

    def scaleAll(sc: => Double) = scale(sc, sc, sc)

    def face(entity: RenderContext => Entity) = customTransformation(new Facing(entity))

    def face(entity: => Entity = null) = customTransformation(new Facing(entity))

    def disableTexture() = glToggle(List(-GL_TEXTURE_2D), GL_ENABLE_BIT)

    def glToggle(properties: List[Int], bit: Int) = customModifier(new GLToggle(properties, bit))

    def customModifier(mod: Modifier): R

    def customTransformation(trans: Transformation): R
  }

}

case class RenderParams(mode: Int = GL_QUADS, modifiers: List[Modifier], transformations: List[Transformation]) extends RPBuilding[RenderParams] {
  val attributeBit = modifiers.foldLeft(0)((acc, m) => acc | m.bit)

  def apply(context: RenderContext): Unit = {
    glPushAttrib(attributeBit)
    modifiers.foreach(_.apply(context))
    transformations.foreach(_.apply(context))
  }

  def unapply(): Unit = {
    modifiers.foreach(_.unapply())
    glPopAttrib()
  }

  override def drawingMode(mode: Int): RenderParams = RenderParams(mode, modifiers, transformations)

  override def customModifier(mod: Modifier) = {
    RenderParams(mode, modifiers ::: mod :: Nil, transformations)
  }

  override def customTransformation(trans: Transformation) = {
    RenderParams(mode, modifiers, transformations ::: trans :: Nil)
  }
}