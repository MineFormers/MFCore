package de.mineformers.core.client.ui.view

import de.mineformers.core.reaction.Event

/**
 * EventControl
 *
 * @author PaleoCrafter
 */
trait EventControl extends View {
  def canReceiveEvent(view: View, event: Event): Boolean
}
