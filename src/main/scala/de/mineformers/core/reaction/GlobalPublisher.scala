package de.mineformers.core.reaction

import de.mineformers.core.reaction.Reactions._

import scala.collection.mutable

/**
 * GlobalPublisher
 *
 * @author PaleoCrafter
 */
trait GlobalPublisher extends GlobalReactor {
  protected val globalListeners = new RefSet[Reaction] {

    import scala.ref._

    protected val underlying = new mutable.HashSet[Reference[Reaction]]

    protected def Ref(a: Reaction) = a match {
      case a: StronglyReferenced => new StrongReference[Reaction](a) with super.Ref[Reaction]
      case _ => new WeakReference[Reaction](a, referenceQueue) with super.Ref[Reaction]
    }
  }

  def publish(e: Event): Unit = {
    for (l <- globalListeners) l(e)
  }

  private[reaction] def subscribeGlobal(listener: Reaction) {
    globalListeners += listener
  }

  private[reaction] def unsubscribeGlobal(listener: Reaction) {
    globalListeners -= listener
  }

  this.listenTo(this)
}
