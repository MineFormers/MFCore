package de.mineformers.core.impl.asm

import de.mineformers.core.asm.transformer.CachedClassTransformer

/**
 * CoreClassTransformer
 *
 * @author PaleoCrafter
 */
class CoreClassTransformer extends CachedClassTransformer {
  /**
   * Registers every transformer
   */
  override def init(): Unit = {
    register(new RenderBlocksTransformer)
    register(new WorldRendererTransformer)
  }
}
