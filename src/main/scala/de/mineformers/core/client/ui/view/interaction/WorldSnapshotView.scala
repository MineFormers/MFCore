package de.mineformers.core.client.ui.view.interaction

import de.mineformers.core.client.renderer.world.{MFWorldRenderer, RenderWorld}
import de.mineformers.core.client.ui.skin.TextureManager
import de.mineformers.core.client.ui.state.{BooleanProperty, ViewState}
import de.mineformers.core.client.ui.util.MouseButton.MouseButton
import de.mineformers.core.client.ui.util.{KeyEvent, MouseButton, MouseEvent}
import de.mineformers.core.client.ui.view.{Drag, View}
import de.mineformers.core.client.util.RenderUtils
import de.mineformers.core.util.math.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.util.math.{Camera, Vector3}
import de.mineformers.core.util.world.RichWorld
import de.mineformers.core.util.world.snapshot.WorldSnapshot
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.command.CommandResultStats.Type
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MathHelper

/**
 * WorldSnapshotView
 *
 * @author PaleoCrafter
 */
class WorldSnapshotView(val snapshot: WorldSnapshot, size0: Size) extends View with Drag {
  val cam = new CameraEntity
  cam.setLocationAndAngles(0, 1.5, 0, -45, 0)
  cam.prevRotationYaw = -45
  cam.prevRotationPitch = 0
  RichWorld(snapshot).clearWorldAccesses()
  val render = new RenderWorld(snapshot)
  snapshot.addWorldAccess(render)
  size = size0
  private var pickedPoint = Vector3(0, 0, 0)

  reactions += {
    case e: MouseEvent.Scroll =>
      cam.setLocationAndAngles(cam.posX, cam.posY- 0.1 * e.direction, cam.posZ , cam.rotationYaw, cam.rotationPitch)
      cam.prevRotationYaw = cam.rotationYaw
      cam.prevRotationPitch = cam.rotationPitch
  }

  globalReactions += {
    case e: KeyEvent.Type =>
      if (e.char == 'l')
        lookAt(Vector3(0, 1.5, 0))
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
        //        lookAt(Vector3.Zero)
      }
  }

  def shadow = state(WorldSnapshotView.PropertyShadow)

  def shadow_=(shadow: Boolean) = state.set(WorldSnapshotView.PropertyShadow, shadow)

  override def defaultState(state: ViewState): Unit = super.defaultState(state.set(WorldSnapshotView.PropertyShadow, false))

  override def dragRegion: Rectangle = screenBounds

  override def update(mousePos: Point): Unit = {
    snapshot.updateEntities()
  }

  def lookAt(pos: Vector3): Unit = {
    val camPos = Vector3(cam.getPositionVector)
    val diff = camPos - pos
    val dist = math.sqrt(diff.z * diff.z + diff.x * diff.x)
    val yaw = math.toDegrees(math.atan2(diff.x, diff.z)).toFloat
    val pitch = math.toDegrees(math.atan2(diff.y, dist)).toFloat

    cam.rotationYaw = yaw - 45
    cam.prevRotationYaw = yaw - 45
    cam.rotationPitch = pitch
    cam.prevRotationPitch = pitch
  }

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
        pickedPoint = RenderUtils.pickedPoint(mousePos, cam.camera.viewport, cam.posZ.toFloat, 0.05F, 256 * MathHelper.SQRT_2)
        RenderUtils.setupOverlayRendering()
      }
      GlStateManager.popMatrix()
      shadow = true
      val drawable = TextureManager(view).orNull
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

    override def setCommandStat(`type`: Type, amount: Int): Unit = getCommandStats.func_179672_a(this, `type`, amount)
  }

}

object WorldSnapshotView {
  final val PropertyShadow = new BooleanProperty("shadow")
}