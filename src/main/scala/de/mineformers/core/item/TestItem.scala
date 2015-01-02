package de.mineformers.core.item

import de.mineformers.core.util.world.snapshot.WorldSnapshot
import de.mineformers.core.util.Implicits.VBlockPos
import de.mineformers.core.util.world.{BlockCuboid, BlockPos}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.world.World

/**
 * TestItem
 *
 * @author PaleoCrafter
 */
class TestItem extends BaseItem("testItem", "testItem", CreativeTabs.tabMisc) {
  var snapshot: WorldSnapshot = _

  override def onItemUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: VBlockPos, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!worldIn.isRemote) {
      if (playerIn.isSneaking && snapshot != null) {
        snapshot.paste(worldIn, BlockPos(0, 1, 0) + pos)
      } else if (!playerIn.isSneaking) {
        snapshot = WorldSnapshot.fromArea(worldIn, BlockCuboid(pos, BlockPos(-2, -2, -2) + pos))
      }
    }
    true
  }
}
