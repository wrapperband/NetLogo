// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.Let;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

// This isn't rejiggered yet because of the extra, unevaluated
// argument. (I say "yet" because this shouldn't be that hard to work
// around.) - ST 2/6/09

public final strictfp class _let
    extends Command {
  public Let let;

  @Override
  public void perform(final Context context)
      throws LogoException {
    context.let(let, args[0].report(context));
    context.ip = next;
  }
}
