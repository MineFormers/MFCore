package de.mineformers.core.reaction

/**
 * GlobalReactor
 *
 * @author PaleoCrafter
 */
trait GlobalReactor {
  val reactions: Reactions = new Reactions.Impl
  val globalReactions: Reactions = new Reactions.Impl

  /**
   * Listen to the given publisher as long as <code>deafTo</code> isn't called for
   * them.
   */
  def listenTo(ps: GlobalPublisher*) = for (p <- ps) p.subscribeGlobal(globalReactions)

  /**
   * Installed reaction won't receive events from the given publisher anylonger.
   */
  def deafTo(ps: GlobalPublisher*) = for (p <- ps) p.unsubscribeGlobal(globalReactions)
}
