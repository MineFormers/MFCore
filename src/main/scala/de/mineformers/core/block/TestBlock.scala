package de.mineformers.core.block

import de.mineformers.core.client.ui.component.container.{Frame, Panel, ScrollPanel, TabbedPanel}
import de.mineformers.core.client.ui.component.decoration.Label
import de.mineformers.core.client.ui.component.interaction.{Button, NumberSpinner, WorldSnapshotView}
import de.mineformers.core.client.util.RenderUtils
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
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util
import net.minecraft.util.{EnumFacing, EnumWorldBlockLayer}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
 * TestBlock
 *
 * @author PaleoCrafter
 */
class TestBlock extends BaseBlock("test123", "test123", CreativeTabs.tabBlock, Material.rock) {
  this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F)

  var snapshot: WorldSnapshot = _

  override def onBlockPlacedBy(worldIn: World, pos: util.BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = if (!worldIn.isRemote) {
    snapshot = WorldSnapshot.fromArea(worldIn, new BlockCuboid(pos, BlockPos(3, -3, 3) + pos))
    new BlockCuboid(BlockPos(0, 0, 0), BlockPos(3, 3, 3)).foreach(p => println(snapshot.getBlockState(p)))
  }

  override def onBlockActivated(worldIn: World, pos: VBlockPos, state: IBlockState, playerIn: EntityPlayer, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (worldIn.isRemote) {
      val panel = new Frame(Size(200, 200))
      val tabs = new TabbedPanel
      tabs.maxSize = Size(Integer.MAX_VALUE, Integer.MAX_VALUE)
      val scroll = new ScrollPanel(Size(59, 50), true, true)
      scroll.maxSize = Size(Integer.MAX_VALUE, Integer.MAX_VALUE)
      val label = new Label("Testy")
      label.tooltip = "Comic Sans MS"
      val text = new NumberSpinner()
      text.position = Point(0, 40)
      label.position = Point(0, 0)
      val button = new Button("test")
      button.size = Size(100, 20)
      button.position = Point(10, 10)
      scroll add text
      scroll add label
      val tab2 = new Panel
      tab2.maxSize = Size(Integer.MAX_VALUE, Integer.MAX_VALUE)
      val snapshotview = new WorldSnapshotView(snapshot, Size(180, 150))
      snapshotview.maxSize = Size(Integer.MAX_VALUE, Integer.MAX_VALUE)
      tab2 add snapshotview
      tabs.addTab("scroll", "Scrolling", scroll)
      tabs.addTab("scroll1", "Test", tab2)
      panel add tabs
      panel.addDefaultControls()
      val proxy = panel.newProxy
      Minecraft.getMinecraft.displayGuiScreen(proxy)
    }
    false
  }

  override def isOpaqueCube: Boolean = {
    false
  }

  /**
   * The type of render function that is called for this block
   */
  override def getRenderType: Int = {
    3
  }

  override def isFullCube: Boolean = {
    false
  }

  @SideOnly(Side.CLIENT) override def getBlockLayer: EnumWorldBlockLayer = {
    EnumWorldBlockLayer.CUTOUT
  }
}
