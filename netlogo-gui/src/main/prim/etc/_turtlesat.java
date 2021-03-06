// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.AgentKindJ;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _turtlesat
    extends Reporter {


  @Override
  public Object report(final Context context)
      throws LogoException {
    double dx = argEvalDoubleValue(context, 0);
    double dy = argEvalDoubleValue(context, 1);
    org.nlogo.agent.Patch patch = null;
    try {
      patch = context.agent.getPatchAtOffsets(dx, dy);
    } catch (org.nlogo.api.AgentException e) {
      return new org.nlogo.agent.ArrayAgentSet(AgentKindJ.Turtle(), 0,
          false);
    }
    if (patch == null) {
      return new org.nlogo.agent.ArrayAgentSet(AgentKindJ.Turtle(), 0,
          false);
    }
    org.nlogo.agent.AgentSet agentset =
        new org.nlogo.agent.ArrayAgentSet
            (AgentKindJ.Turtle(), patch.turtleCount(),
                false);
    for (org.nlogo.agent.Turtle turtle : patch.turtlesHere()) {
      if (turtle != null) {
        agentset.add(turtle);
      }
    }
    return agentset;
  }
}
