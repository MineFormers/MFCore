package de.mineformers.core.client.ui.component.interaction

import de.mineformers.core.client.renderer.world.{MFWorldRenderer, RenderWorld}
import de.mineformers.core.client.ui.component.{Drag, View}
import de.mineformers.core.client.ui.skin.TextureManager
import de.mineformers.core.client.ui.state.{ComponentState, BooleanProperty}
import de.mineformers.core.client.ui.util.MouseButton.MouseButton
import de.mineformers.core.client.ui.util.{MouseButton, MouseEvent}
import de.mineformers.core.client.util.RenderUtils
import de.mineformers.core.util.math.Camera
import de.mineformers.core.util.math.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.util.world.RichWorld
import de.mineformers.core.util.world.snapshot.WorldSnapshot
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound

/**
 * WorldSnapshotView
 *
 * @author PaleoCrafter
 */
class WorldSnapshotView(val snapshot: WorldSnapshot, size0: Size) extends View with Drag {
  val cam = new CameraEntity
  cam.setLocationAndAngles(0, 0, 0, -180, 0)
  cam.prevRotationYaw = -180
  cam.prevRotationPitch = 0
  RichWorld(snapshot).clearWorldAccesses()
  val render = new RenderWorld(snapshot)
  snapshot.addWorldAccess(render)
  size = size0

  reactions += {
    case e: MouseEvent.Scroll =>
      cam.setLocationAndAngles(cam.posX, cam.posY, cam.posZ - 0.1 * e.direction, cam.rotationYaw, cam.rotationPitch)
      cam.prevRotationYaw = cam.rotationYaw
      cam.prevRotationPitch = cam.rotationPitch
  }

  override def canDrag: Boolean = true

  override def onDrag(region: String, newPos: Point, lastPos: Point, delta: Point, button: MouseButton): Unit = region match {
    case "full" =>
      if (button == MouseButton.Left) {
        cam.setLocationAndAngles(cam.posX + delta.x * 0.1, cam.posY + delta.y * 0.1, cam.posZ, cam.rotationYaw, cam.rotationPitch)
        cam.prevRotationYaw = cam.rotationYaw
        cam.prevRotationPitch = cam.rotationPitch
      } else if (button == MouseButton.Right) {
        cam.rotationYaw -= delta.x * 0.1f
        cam.rotationPitch -= delta.y * 0.1f
        cam.prevRotationYaw = cam.rotationYaw
        cam.prevRotationPitch = cam.rotationPitch
      }
  }
  
  def shadow = state(WorldSnapshotView.PropertyShadow)
  
  def shadow_=(shadow: Boolean) = state.set(WorldSnapshotView.PropertyShadow, shadow)

  override def defaultState(state: ComponentState): Unit = super.defaultState(state.set(WorldSnapshotView.PropertyShadow, false))

  override def dragRegion: Rectangle = screenBounds

  override def update(mousePos: Point): Unit = snapshot.updateEntities()

  override var skin: Skin = new WorldSnapshotSkin

  override def dispose(): Unit = {
    super.dispose()
    render.dispose()
    snapshot.removeWorldAccess(render)
  }

  class WorldSnapshotSkin extends Skin {
    override protected def drawForeground(mousePos: Point): Unit = {
      GlStateManager.pushMatrix()
      if (cam.updateCamera(utils.revertScale(Rectangle(screenBounds.start + Point(1, 1), screenBounds.end - Point(1, 1))))) {
        RenderUtils.applyCamera(cam.camera, cam.getPositionVector)
        MFWorldRenderer.renderWorld(cam, snapshot)
        RenderUtils.setupOverlayRendering()
      }
      GlStateManager.popMatrix()
      shadow = true
      val drawable = TextureManager(component).orNull
      drawable.size = size
      drawable.draw(mousePos, screen, zIndex)
      shadow = false
    }
  }

  class CameraEntity extends Entity(snapshot) {
    val camera = new Camera(null, null, null)

    def updateCamera(viewport: Rectangle): Boolean = {
      camera.viewport = viewport
      //      camera.projectionMatrix = MathUtils.createProjectionMatrixAsPerspective(30, 0.05, 50, viewport.width, viewport.height)
      //      camera.viewMatrix = MathUtils.createMatrixAsLookAt(getPositionVector, Vector3.Zero, Vector3(0, 1, 0))
      camera.isValid
      true
    }

    override def entityInit(): Unit = ()

    override def writeEntityToNBT(tagCompound: NBTTagCompound): Unit = ()

    override def readEntityFromNBT(tagCompound: NBTTagCompound): Unit = ()
  }

}

object WorldSnapshotView {
  final val PropertyShadow = new BooleanProperty("shadow")

  List(0, 0, 0).find()
}