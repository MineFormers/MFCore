package de.mineformers.core.client.renderer

import de.mineformers.core.structure.StructureWorld
import de.mineformers.core.util.MathUtils
import de.mineformers.core.util.renderer.ShaderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.culling.Frustrum
import net.minecraft.entity.Entity
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20._

/**
 * StructureWorldRenderer
 *
 * @author PaleoCrafter
 */
class StructureWorldRenderer() {
  final val vertex =
    """#version 120
      |
      |void main() {
      | gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
      | gl_TexCoord[0] = gl_MultiTexCoord0;
      | gl_FrontColor = gl_Color;
      |}
    """.stripMargin
  final val fragment =
    """#version 120
      |uniform float alpha; uniform sampler2D tex;
      |
      |void main() {
      | gl_FragColor = texture2D(tex, vec2(gl_TexCoord[0])) * gl_Color * vec4(1.0, 1.0, 1.0, alpha);
      |}
    """.stripMargin
  val shaders = new ShaderSystem((vertex, GL_VERTEX_SHADER), (fragment, GL_FRAGMENT_SHADER))

  def render(world: StructureWorld, partialTicks: Float): Unit = {
    val x = world.pos.x
    val y = world.pos.y
    val z = world.pos.z
    val frustrum = new Frustrum()
    val cam = Minecraft.getMinecraft.renderViewEntity
    val camX: Double = cam.lastTickPosX + (cam.posX - cam.lastTickPosX) * partialTicks
    val camY: Double = cam.lastTickPosY + (cam.posY - cam.lastTickPosY) * partialTicks
    val camZ: Double = cam.lastTickPosZ + (cam.posZ - cam.lastTickPosZ) * partialTicks
    frustrum.setPosition(camX, camY, camZ)
    if (frustrum.isBoundingBoxInFrustum(world.bounds)) {
      val step: Float = MathUtils.pulsate(Minecraft.getSystemTime % 10000, 0, 1, 10000).toFloat
      GL11.glPushMatrix()
      GL11.glEnable(GL11.GL_BLEND)
      GL11.glDisable(GL11.GL_ALPHA_TEST)
      translateToWorldCoords(Minecraft.getMinecraft.renderViewEntity, partialTicks)
      GL11.glTranslatef(x, y, z)
      GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)
      shaders.activate()
      shaders.setUniform1i("tex", 0)
      shaders.setUniform1f("alpha", step)
      val list = world.localWorldAccess.getChunkRenderers
      for (pass <- 0 until 2)
        list foreach {
          _.render(pass)
        }
      shaders.deactivate()
      GL11.glEnable(GL11.GL_ALPHA_TEST)
      GL11.glDisable(GL11.GL_BLEND)
      GL11.glPopMatrix()
    }
  }

  def translateToWorldCoords(entity: Entity, frame: Float): Unit = {
    val interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * frame
    val interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * frame
    val interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * frame

    GL11.glTranslated(-interpPosX, -interpPosY, -interpPosZ)
  }
}
