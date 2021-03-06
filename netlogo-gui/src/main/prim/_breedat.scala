// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.AgentKind
import scala.collection.JavaConverters._

import org.nlogo.agent.{ ArrayAgentSet, Patch, Turtle }
import org.nlogo.api.{ AgentException}
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _breedat(breedName: String) extends Reporter {


  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context): AnyRef = {
    val dx = argEvalDoubleValue(context, 0)
    val dy = argEvalDoubleValue(context, 1)
    var patch: Patch = null
    try {
      patch = context.agent.getPatchAtOffsets(dx, dy)
    } catch {
      case e: AgentException =>
        return new ArrayAgentSet(AgentKind.Turtle, 0, false)
    }
    if (patch == null)
      return new ArrayAgentSet(AgentKind.Turtle, 0, false)
    val breed = world.getBreed(breedName)
    new ArrayAgentSet(
      AgentKind.Turtle,
      patch.turtlesHere.asScala.
        filter(turtle => turtle != null && turtle.getBreed == breed).toArray)
  }
}
