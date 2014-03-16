package de.mineformers.core.block

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity

import net.minecraft.world.World
import cpw.mods.fml.common.Loader
import net.minecraft.creativetab.CreativeTabs

class BaseBlock(name: String, texture: String, tab: CreativeTabs, material: Material, teClass: Class[_ <: TileEntity] = null) extends Block(material) {

  def this(name: String, tab: CreativeTabs, material: Material, teClass: Class[_ <: TileEntity] = null) = this(name, name, tab, material, teClass)

  this.setBlockName(name)
  this.setCreativeTab(tab)
  this.setBlockTextureName(Loader.instance().activeModContainer().getModId.toLowerCase + ":" + texture)

  override def hasTileEntity(meta: Int): Boolean = {
    teClass != null
  }

  override def createTileEntity(world: World, meta: Int): TileEntity = if (hasTileEntity(meta)) teClass.newInstance() else null

}
