package de.mineformers.core.client.renderer.world

import de.mineformers.core.client.util.RenderUtils
import de.mineformers.core.util.math.Vector3
import de.mineformers.core.util.world.RichWorld
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.culling.{ClippingHelperImpl, Frustum}
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.{ActiveRenderInfo, GLAllocation, GlStateManager, RenderHelper}
import net.minecraft.entity.Entity
import net.minecraft.util.{EnumWorldBlockLayer, MathHelper, Vec3}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.ReflectionHelper
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU

/**
 * MFWorldRenderer
 *
 * @author PaleoCrafter
 */
object MFWorldRenderer {
  private var lastRender = 0L
  private val positionField = ReflectionHelper.findField(classOf[ActiveRenderInfo], "position", "field_73021_x")
  private val rotXField = ReflectionHelper.findField(classOf[ActiveRenderInfo], "rotationX", "field_73021_x")
  private val rotZField = ReflectionHelper.findField(classOf[ActiveRenderInfo], "rotationZ", "field_73021_x")
  private val rotYZField = ReflectionHelper.findField(classOf[ActiveRenderInfo], "rotationYZ", "field_73021_x")
  private val rotXYField = ReflectionHelper.findField(classOf[ActiveRenderInfo], "rotationXY", "field_73021_x")
  private val rotXZField = ReflectionHelper.findField(classOf[ActiveRenderInfo], "rotationXZ", "field_73021_x")
  private final val viewPortBuffer = GLAllocation.createDirectIntBuffer(16)
  private final val modelViewBuffer = GLAllocation.createDirectFloatBuffer(16)
  private final val projectionBuffer = GLAllocation.createDirectFloatBuffer(16)
  private final val unprojectionBuffer = GLAllocation.createDirectFloatBuffer(3)

  def renderWorld(viewEntity: Entity, world: World): Unit = {
    val render = RichWorld(world).worldAccesses.find(_.isInstanceOf[RenderWorld])
    if (render.isDefined) {
      val oldEntity = RenderUtils.mc.getRenderViewEntity
      RenderUtils.mc.setRenderViewEntity(viewEntity)
      val i = Math.max(Minecraft.getDebugFPS, 30)
      this.renderWorld(viewEntity, world, render.get.asInstanceOf[RenderWorld], RenderUtils.partialTicks, lastRender + 1000000000L / i)
      lastRender = System.nanoTime()
      RenderUtils.mc.setRenderViewEntity(oldEntity)
    }
  }

  def renderWorld(viewEntity: Entity, world: World, render: RenderWorld, partialTicks: Float, time: Long): Unit = {
    GlStateManager.enableDepth()
    GlStateManager.enableAlpha()
    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F)
    GlStateManager.rotate(viewEntity.prevRotationPitch + (viewEntity.rotationPitch - viewEntity.prevRotationPitch) * partialTicks, 1.0F, 0.0F, 0.0F)
    GlStateManager.rotate(viewEntity.prevRotationYaw + (viewEntity.rotationYaw - viewEntity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F)
    this.renderWorldPass(2, viewEntity, world, render, partialTicks, time)
  }

  private def renderWorldPass(pass: Int, viewEntity: Entity, world: World, render: RenderWorld, partialTicks: Float, time: Long): Unit = {
    updateRenderInfo(viewEntity)
    ClippingHelperImpl.getInstance()
    val frustum = new Frustum
    val pos = RenderUtils.calculateCamPosition(viewEntity)
    frustum.setPosition(pos.x, pos.y, pos.z)

    RenderUtils.mc.getTextureManager.bindTexture(TextureMap.locationBlocksTexture)
    RenderHelper.disableStandardItemLighting()

    render.setupTerrain(viewEntity, partialTicks, frustum, 0)

    if (pass == 0 || pass == 2) {
      render.updateChunks(time)
    }

    GlStateManager.matrixMode(GL11.GL_MODELVIEW)
    GlStateManager.pushMatrix()
    GlStateManager.disableAlpha()
    render.renderBlockLayer(EnumWorldBlockLayer.SOLID, viewEntity)
    GlStateManager.enableAlpha()
    render.renderBlockLayer(EnumWorldBlockLayer.CUTOUT_MIPPED, viewEntity)
    RenderUtils.mc.getTextureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false)
    render.renderBlockLayer(EnumWorldBlockLayer.CUTOUT, viewEntity)
    RenderUtils.mc.getTextureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap()
    GlStateManager.shadeModel(GL11.GL_FLAT)
    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F)

    GlStateManager.matrixMode(GL11.GL_MODELVIEW)
    GlStateManager.popMatrix()
    GlStateManager.pushMatrix()
    RenderHelper.enableStandardItemLighting()
    net.minecraftforge.client.ForgeHooksClient.setRenderPass(0)
    render.renderTileEntities(viewEntity, frustum, partialTicks)
    net.minecraftforge.client.ForgeHooksClient.setRenderPass(0)
    RenderHelper.disableStandardItemLighting()
    GlStateManager.matrixMode(GL11.GL_MODELVIEW)
    GlStateManager.popMatrix()
    GlStateManager.pushMatrix()

    GlStateManager.enableBlend()
    GlStateManager.depthMask(false)
    RenderUtils.mc.getTextureManager.bindTexture(TextureMap.locationBlocksTexture)
    GlStateManager.shadeModel(GL11.GL_SMOOTH)
    if (RenderUtils.mc.gameSettings.fancyGraphics) {
      GlStateManager.enableBlend()
      GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
      render.renderBlockLayer(EnumWorldBlockLayer.TRANSLUCENT, viewEntity)
      GlStateManager.disableBlend()
    }
    else {
      render.renderBlockLayer(EnumWorldBlockLayer.TRANSLUCENT, viewEntity)
    }

    GlStateManager.matrixMode(GL11.GL_MODELVIEW)
    GlStateManager.popMatrix()
    GlStateManager.pushMatrix()
    RenderHelper.enableStandardItemLighting()
    net.minecraftforge.client.ForgeHooksClient.setRenderPass(1)
    render.renderTileEntities(viewEntity, frustum, partialTicks)
    net.minecraftforge.client.ForgeHooksClient.setRenderPass(-1)
    RenderHelper.disableStandardItemLighting()
    GlStateManager.matrixMode(GL11.GL_MODELVIEW)
    GlStateManager.popMatrix()
  }

  private def updateRenderInfo(entity: Entity): Unit = {
    positionField.setAccessible(true)
    rotXField.setAccessible(true)
    rotZField.setAccessible(true)
    rotYZField.setAccessible(true)
    rotXYField.setAccessible(true)
    rotXZField.setAccessible(true)

    GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer)
    GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, projectionBuffer)
    GL11.glGetInteger(GL11.GL_VIEWPORT, viewPortBuffer)
    val viewX = ((viewPortBuffer.get(0) + viewPortBuffer.get(2)) / 2).toFloat
    val viewY = ((viewPortBuffer.get(1) + viewPortBuffer.get(3)) / 2).toFloat
    GLU.gluUnProject(viewX, viewY, 0.0F, modelViewBuffer, projectionBuffer, viewPortBuffer, unprojectionBuffer)
    val position = new Vec3(unprojectionBuffer.get(0), unprojectionBuffer.get(1), unprojectionBuffer.get(2))
    val pitch: Float = entity.rotationPitch
    val yaw: Float = entity.rotationYaw
    val rotationX = MathHelper.cos(yaw * Vector3.Rad)
    val rotationZ = MathHelper.sin(yaw * Vector3.Rad)
    val rotationYZ = -rotationZ * MathHelper.sin(pitch * Vector3.Rad)
    val rotationXY = rotationX * MathHelper.sin(pitch * Vector3.Rad)
    val rotationXZ = MathHelper.cos(pitch * Vector3.Rad)
    positionField.set(null, position)
    rotXField.set(null, rotationX)
    rotZField.set(null, rotationZ)
    rotYZField.set(null, rotationYZ)
    rotXYField.set(null, rotationXY)
    rotXZField.set(null, rotationXZ)
  }
}
