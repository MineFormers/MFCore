package de.mineformers.core.client.renderer

import de.mineformers.core.client.util.RenderUtils
import net.minecraft.entity.Entity
import org.lwjgl.opengl.GL11._

class Transformation(val apply: RenderContext => Unit)

object Translation {
  def center(x: RenderContext => Double, y: RenderContext => Double, z: RenderContext => Double) = new Translation(x.andThen(_ + 0.5), y.andThen(_ + 0.5), z.andThen(_ + 0.5))

  def center(x: => Double = 0D, y: => Double = 0D, z: => Double = 0D) = new Translation(x + 0.5, y + 0.5, z + 0.5)
}

class Translation(x: RenderContext => Double, y: RenderContext => Double, z: RenderContext => Double) extends Transformation(c => glTranslated(x(c), y(c), z(c))) {
  def this(x: => Double, y: => Double, z: => Double) = this(c => x, c => y, c => z)
}

object Rotation {
  def aroundX(angle: => Double) = new Rotation(angle, 1, 0, 0)

  def aroundY(angle: => Double) = new Rotation(angle, 0, 1, 0)

  def aroundZ(angle: => Double) = new Rotation(angle, 0, 0, 1)

  def aroundX(angle: RenderContext => Double) = new Rotation(angle, c => 1, c => 0, c => 0)

  def aroundY(angle: RenderContext => Double) = new Rotation(angle, c => 0, c => 1, c => 0)

  def aroundZ(angle: RenderContext => Double) = new Rotation(angle, c => 0, c => 0, c => 1)
}

class Rotation(angle: RenderContext => Double, x: RenderContext => Double, y: RenderContext => Double, z: RenderContext => Double) extends Transformation(c => glRotated(angle(c), x(c), y(c), z(c))) {
  def this(angle: => Double, x: => Double, y: => Double, z: => Double) = this(c => angle, c => x, c => y, c => z)
}

class Scaling(x: RenderContext => Double, y: RenderContext => Double, z: RenderContext => Double) extends Transformation(c => glScaled(x(c), y(c), z(c))) {
  def this(x: => Double, y: => Double, z: => Double) = this(c => x, c => y, c => z)
}

class Facing(entity: RenderContext => Entity) extends Transformation(c => {
  if (entity(c) != null)
    RenderUtils.rotateFacing(entity(c))
  else
    RenderUtils.rotateFacing()
}) {
  def this(entity: => Entity = null) = this(c => entity)
}