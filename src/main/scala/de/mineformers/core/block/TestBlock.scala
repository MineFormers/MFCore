package de.mineformers.core.block

import de.mineformers.core.client.ui.util.ViewEvent.ButtonPressed
import de.mineformers.core.client.ui.view.container.dialog.Dialog
import de.mineformers.core.client.ui.view.container.{Frame, Panel, ScrollPanel, TabbedPanel}
import de.mineformers.core.client.ui.view.interaction.{Button, NumberSpinner}
import de.mineformers.core.reaction.Listener
import de.mineformers.core.util.Implicits.VBlockPos
import de.mineformers.core.util.math.shape2d.{Point, Size}
import de.mineformers.core.util.world.snapshot.WorldSnapshot
import de.mineformers.core.util.world.{BlockCuboid, BlockPos}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util
import net.minecraft.util.{EnumFacing, EnumWorldBlockLayer}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
 * TestBlock
 *
 * @author PaleoCrafter
 */
class TestBlock extends BaseBlock("test123", "test123", CreativeTabs.tabBlock, Material.rock) {
  this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1F, 1.0F)
//  setLightLevel(1)

  var snapshot: WorldSnapshot = _

  override def onBlockPlacedBy(worldIn: World, pos: util.BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = if (!worldIn.isRemote) {
    snapshot = WorldSnapshot.fromArea(worldIn, new BlockCuboid(pos, BlockPos(3, -3, 3) + pos))
  }

  override def getMixedBrightnessForBlock(worldIn: IBlockAccess, pos: VBlockPos): Int = {
    println(super.getMixedBrightnessForBlock(worldIn, pos))
    15728640
  }

  override def onBlockActivated(worldIn: World, pos: VBlockPos, state: IBlockState, playerIn: EntityPlayer, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (worldIn.isRemote) {
      val panel = new Frame(Size(200, 200))
      val tabs = new TabbedPanel
      tabs.maxSize = Size(Integer.MAX_VALUE, Integer.MAX_VALUE)
      val scroll = new ScrollPanel(Size(59, 50), true, true)
      scroll.maxSize = Size(Integer.MAX_VALUE, Integer.MAX_VALUE)
      val btnDialog = new Button("Show Dialog")
      val text = new NumberSpinner()
      text.position = Point(0, 40)
      scroll add text
      btnDialog.position = Point(5, 5)
      scroll add btnDialog
      val tab2 = new Panel
      tab2.maxSize = Size(Integer.MAX_VALUE, Integer.MAX_VALUE)
      tabs.addTab("scroll", "Scrolling", scroll)
      tabs.addTab("scroll1", "Test", tab2)
      panel add tabs

      val proxy = panel.newProxy
      proxy.listeners += Listener.reaction({
        case ButtonPressed(b) =>
          if(b eq btnDialog) {
            val dialog = Dialog.confirm("Test", "Test1")
            dialog.modal = true
            dialog.display(b.context)
          }
      }).listenTo(btnDialog)
      Minecraft.getMinecraft.displayGuiScreen(proxy)
    }
    false
  }

  override def isOpaqueCube: Boolean = false

  /**
   * The type of render function that is called for this block
   */
  override def getRenderType: Int = 3

  override def isFullCube: Boolean =false

  override def isReplaceable(worldIn: World, pos: util.BlockPos): Boolean = true

  @SideOnly(Side.CLIENT) override def getBlockLayer: EnumWorldBlockLayer = {
    EnumWorldBlockLayer.CUTOUT
  }
}
