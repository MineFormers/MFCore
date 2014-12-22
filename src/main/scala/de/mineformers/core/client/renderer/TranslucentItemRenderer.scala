package de.mineformers.core.client.renderer

import de.mineformers.core.client.util.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.renderer.{ItemRenderer, OpenGlHelper, Tessellator}
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.{ItemRenderType, ItemRendererHelper}
import org.lwjgl.opengl.{GL11, GL12}

/**
 * TranslucentItemRenderer
 *
 * @author PaleoCrafter
 */
class TranslucentItemRenderer extends IItemRenderer {
  private final val ResItemGlint: ResourceLocation = new ResourceLocation("textures/misc/enchanted_item_glint.png")

  override def handleRenderType(item: ItemStack, `type`: ItemRenderType): Boolean = `type` != ItemRenderType.INVENTORY

  override def shouldUseRenderHelper(`type`: ItemRenderType, item: ItemStack, helper: ItemRendererHelper): Boolean = helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.ENTITY_BOBBING

  import net.minecraftforge.client.IItemRenderer.ItemRenderType._

  override def renderItem(`type`: ItemRenderType, item: ItemStack, data: AnyRef*): Unit = `type` match {
    case ENTITY =>
      GL11.glPushMatrix()
      GL11.glTranslatef(-0.5F, 0F, 0F)
      if (item.isOnItemFrame)
        GL11.glTranslatef(0F, -0.3F, 0.01F)
      render(item)
      GL11.glPopMatrix()
    case EQUIPPED | EQUIPPED_FIRST_PERSON => render(item)
    case _ =>
  }

  private def render(item: ItemStack): Unit = {
    GL11.glPushMatrix()
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    if (item.getItem.requiresMultipleRenderPasses) {
      for (pass <- 0 until item.getItem.getRenderPasses(item.getItemDamage)) {
        val color = item.getItem.getColorFromItemStack(item, pass)
        val r = (color >> 16 & 255) / 255.0F
        val g = (color >> 8 & 255) / 255.0F
        val b = (color & 255) / 255.0F
        GL11.glColor4f(r, g, b, 1.0F)
        renderPass(item, pass)
      }
    }
    else {
      val color = item.getItem.getColorFromItemStack(item, 0)
      val r = (color >> 16 & 255) / 255.0F
      val g = (color >> 8 & 255) / 255.0F
      val b = (color & 255) / 255.0F
      GL11.glColor4f(r, g, b, 1.0F)
      renderPass(item, 0)
    }
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glPopMatrix()
    GL11.glColor4f(1F, 1F, 1F, 1F)
  }

  def renderPass(stack: ItemStack, pass: Int): Unit = {
    val player = Minecraft.getMinecraft.thePlayer
    val icon = player.getItemIcon(stack, pass)

    if (icon == null) {
      GL11.glPopMatrix()
      return
    }
    RenderUtils.bindTexture(RenderUtils.mc.getTextureManager.getResourceLocation(stack.getItemSpriteNumber))
    TextureUtil.func_152777_a(false, false, 1.0F)
    val tessellator = Tessellator.instance
    val minU = icon.getMinU
    val maxU = icon.getMaxU
    val minV = icon.getMinV
    val maxV = icon.getMaxV
    GL11.glEnable(GL12.GL_RESCALE_NORMAL)
    ItemRenderer.renderItemIn2D(tessellator, maxU, minV, minU, maxV, icon.getIconWidth, icon.getIconHeight, 0.0625F)
    if (stack.hasEffect(pass)) {
      GL11.glDepthFunc(GL11.GL_EQUAL)
      GL11.glDisable(GL11.GL_LIGHTING)
      RenderUtils.bindTexture(ResItemGlint)
      GL11.glEnable(GL11.GL_BLEND)
      OpenGlHelper.glBlendFunc(768, 1, 1, 0)
      val color = 0.76F
      GL11.glColor4f(0.5F * color, 0.25F * color, 0.8F * color, 1.0F)
      GL11.glMatrixMode(GL11.GL_TEXTURE)
      GL11.glPushMatrix()
      val scale = 0.125F
      GL11.glScalef(scale, scale, scale)
      var offset = (Minecraft.getSystemTime % 3000L) / 3000.0F * 8.0F
      GL11.glTranslatef(offset, 0.0F, 0.0F)
      GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F)
      ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F)
      GL11.glPopMatrix()
      GL11.glPushMatrix()
      GL11.glScalef(scale, scale, scale)
      offset = (Minecraft.getSystemTime % 4873L) / 4873.0F * 8.0F
      GL11.glTranslatef(-offset, 0.0F, 0.0F)
      GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F)
      ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F)
      GL11.glPopMatrix()
      GL11.glMatrixMode(GL11.GL_MODELVIEW)
      GL11.glDisable(GL11.GL_BLEND)
      GL11.glEnable(GL11.GL_LIGHTING)
      GL11.glDepthFunc(GL11.GL_LEQUAL)
    }
    GL11.glDisable(GL12.GL_RESCALE_NORMAL)
    RenderUtils.bindTexture(RenderUtils.mc.getTextureManager.getResourceLocation(stack.getItemSpriteNumber))
    TextureUtil.func_147945_b()
  }
}
