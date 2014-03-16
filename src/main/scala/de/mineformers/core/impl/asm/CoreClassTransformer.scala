package de.mineformers.core.impl.asm

import de.mineformers.core.asm.transformer.CachedClassTransformer

class CoreClassTransformer extends CachedClassTransformer {
  override def init(): Unit = {
    register(new RenderBlocksTransformer)
    register(new WorldRendererTransformer)
  }
}
