package de.mineformers.core.reaction

/**
 * Listener
 *
 * @author PaleoCrafter
 */
class Listener private(reactions0: Seq[Reactions.Reaction], publishers: Seq[GlobalPublisher]) extends GlobalReactor {
  reactions0 foreach globalReactions.+=

  listenTo(publishers: _*)
}

object Listener {

  class Builder private[Listener]() {
    private val reactions = Seq.newBuilder[Reactions.Reaction]

    def reaction(reaction: Reactions.Reaction): Builder = {
      reactions += reaction
      this
    }

    def listenTo(publishers: GlobalPublisher*) = new Listener(reactions.result(), publishers)
  }

  def reaction(reaction: Reactions.Reaction) = new Builder().reaction(reaction)
}
