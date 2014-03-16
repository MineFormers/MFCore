package de.mineformers.core.registry

import cpw.mods.fml.common.{LoaderState, Loader}
import net.minecraft.item.ItemBlock
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.block.Block
import de.mineformers.core.block.MetaBlock
import de.mineformers.core.item.ItemBlockMeta

case class BlockEntry(
                       block: Block,
                       itemBlock: Class[_ <: ItemBlock] = classOf[ItemBlock],
                       modId: String = null, cstrArgs: Array[Object] = Array[Object]())

object SharedBlockRegistry extends SharedRegistry[String, BlockEntry] {

  def add(
           key: String,
           block: Block,
           itemBlock: Class[_ <: ItemBlock] = classOf[ItemBlock],
           modId: String = null, cstrArgs: Array[Object] = Array[Object]()): Unit = this.add(key, BlockEntry(block, itemBlock, modId, cstrArgs))

  override protected def put(key: String, value: BlockEntry): Unit = {
    if (Loader.instance().isInState(LoaderState.PREINITIALIZATION)) {
      if (value.block.isInstanceOf[MetaBlock] && value.itemBlock == classOf[ItemBlock]) {
        GameRegistry.registerBlock(value.block, classOf[ItemBlockMeta], key, value.modId)
        return
      }
      if (value.cstrArgs.length > 0)
        GameRegistry.registerBlock(value.block, value.itemBlock, key, value.modId, value.cstrArgs: _*)
      else
        GameRegistry.registerBlock(value.block, value.itemBlock, key, value.modId)
    } else {
      System.err.println("A mod was trying to register a block outside the pre init phase")
    }
  }
}
