// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ Dump, ReporterRunnable}
import org.nlogo.core.Syntax
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Context, EngineException, Reporter }
import org.nlogo.window.GUIWorkspace

class _userinput extends Reporter {



  override def report(context: Context) = {
    val inputMessage = args(0).report(context)
    workspace match {
      case gw: GUIWorkspace =>
        gw.updateUI()
        val result = workspace.waitForResult(
          new ReporterRunnable[String] {
            override def run() = {
              gw.view.mouseDown(false)
              new org.nlogo.swing.InputDialog(
                gw.getFrame, "User Input", Dump.logoObject(inputMessage),
                I18N.gui.fn).showInputDialog()
            }})
        Option(result).getOrElse(
          throw new org.nlogo.nvm.HaltException(true))
      case _ =>
        throw new EngineException(
          context, this, "You can't get user input headless.")
    }
  }

}
