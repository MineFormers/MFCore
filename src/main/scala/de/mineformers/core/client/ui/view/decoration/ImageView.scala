package de.mineformers.core.client.ui.view.decoration

import de.mineformers.core.client.ui.view.View
import de.mineformers.core.util.ResourceUtils.Resource
import de.mineformers.core.util.math.shape2d.{Point, Size}
import org.lwjgl.opengl.GL11

/**
 * ImageView
 *
 * @author PaleoCrafter
 */
class ImageView(image: Resource, size0: Size, uv: Point = Point(0, 0), var textureSize: Size = null) extends View {
  size = size0

  override def update(mousePos: Point): Unit = ()

  override var skin: Skin = _

  class TextSkin extends Skin {
    def drawForeground(mousePos: Point): Unit = {
      GL11.glColor4f(1, 1, 1, 1)
      utils.drawRectangle(image, screen.x, screen.y, zIndex, size.width, size.height, uv.x, uv.y, textureSize.width, textureSize.height)
    }
  }

}
