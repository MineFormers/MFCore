package de.mineformers.core.client.ui.component.interaction

import de.mineformers.core.client.renderer.world.{MFWorldRenderer, RenderWorld}
import de.mineformers.core.client.ui.component.View
import de.mineformers.core.client.util.RenderUtils
import de.mineformers.core.util.MathUtils
import de.mineformers.core.util.math.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.util.math.{Camera, Vector3}
import de.mineformers.core.util.renderer.GuiUtils
import de.mineformers.core.util.world.RichWorld
import de.mineformers.core.util.world.snapshot.WorldSnapshot
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import org.lwjgl.opengl.GL11

/**
 * WorldSnapshotView
 *
 * @author PaleoCrafter
 */
class WorldSnapshotView(val snapshot: WorldSnapshot, size0: Size) extends View {
  val cam = new CameraEntity
  cam.setLocationAndAngles(0, 0, 20, -180, 0)
  cam.prevRotationYaw = -180
  RichWorld(snapshot).clearWorldAccesses()
  snapshot.addWorldAccess(new RenderWorld(snapshot))
  size = size0

  override def update(mousePos: Point): Unit = ()

  override var skin: Skin = new WorldSnapshotSkin

  class WorldSnapshotSkin extends Skin {
    override protected def drawForeground(mousePos: Point): Unit = {
        GlStateManager.pushMatrix()
        if (cam.updateCamera(utils.revertScale(screenBounds))) {
          RenderUtils.applyCamera(cam.camera, cam.getPositionVector)
          MFWorldRenderer.renderWorld(cam, snapshot)
          RenderUtils.setupOverlayRendering()
        }
        GlStateManager.popMatrix()
    }
  }

  class CameraEntity extends Entity(snapshot) {
    val camera = new Camera(null, null, null)

    def updateCamera(viewport: Rectangle): Boolean = {
      camera.viewport = viewport
      camera.projectionMatrix = MathUtils.createProjectionMatrixAsPerspective(30, 0.05, 50, viewport.width, viewport.height)
      camera.viewMatrix = MathUtils.createMatrixAsLookAt(getPositionVector, Vector3.Zero, Vector3(0, 1, 0))
      camera.isValid
    }

    override def entityInit(): Unit = ()

    override def writeEntityToNBT(tagCompound: NBTTagCompound): Unit = ()

    override def readEntityFromNBT(tagCompound: NBTTagCompound): Unit = ()
  }

}
