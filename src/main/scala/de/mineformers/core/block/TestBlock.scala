package de.mineformers.core.block

import de.mineformers.core.client.shape2d.{Size, Point}
import de.mineformers.core.client.ui.component.container.{ScrollPanel, Frame}
import de.mineformers.core.client.ui.component.decoration.Label
import de.mineformers.core.client.ui.component.interaction.{Button, TextBox}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.{BlockPos, EnumFacing}
import net.minecraft.world.World

/**
 * TestBlock
 *
 * @author PaleoCrafter
 */
class TestBlock extends BaseBlock("test123", "test123", CreativeTabs.tabBlock, Material.rock) {
  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (worldIn.isRemote) {
      val panel = new Frame(Size(100, 100))
      val scroll = new ScrollPanel(Size(92, 88), true, false)
      val label = new Label("test")
      val text = new TextBox("test", Size(50, 20))
      text.position = Point(0, 40)
      label.position = Point(-10, 5)
      val button = new Button("test")
      button.size = Size(100, 20)
      button.position = Point(10, 10)
      scroll add text
      scroll add label
      scroll add button
      panel add scroll
      panel.addDefaultControls()
      val proxy = panel.newProxy
      Minecraft.getMinecraft.displayGuiScreen(proxy)
    }
    false
  }
}
