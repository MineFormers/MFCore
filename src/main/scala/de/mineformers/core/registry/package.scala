package de.mineformers.core

package object registry {

  object MFBlocks {
    def apply(key: String): BlockEntry = SharedBlockRegistry(key)
  }

  object MFItems {
    def apply(key: String): ItemEntry = SharedItemRegistry(key)
  }

}
