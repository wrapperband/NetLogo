// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.nio.file.Paths

import org.scalatest.FunSuite

import org.nlogo.core.{ DummyCompilationEnvironment, DummyExtensionManager, Femto, LiteralParser }
import org.nlogo.api.Version
import org.nlogo.util.SlowTest
import org.nlogo.fileformat, fileformat.NLogoThreeDFormat

class TestModelNetLogoVersions extends FunSuite with SlowTest {
  val paths = ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions")
  val dummyWorkspace = new DummyWorkspace()
  val extensionManager = new ExtensionManager(dummyWorkspace, new JarLoader(dummyWorkspace))
  for(path <- paths)
    test(path, SlowTest.Tag) {
      // if this test suddenly starts failing, make sure all models are the most current version
      val loader =
        fileformat.basicLoader
          .addFormat[Array[String], NLogoThreeDFormat](new NLogoThreeDFormat(fileformat.defaultAutoConversion))

      val model = loader.readModel(Paths.get(path).toUri).get
      assert(Version.compatibleVersion(model.version))
    }
}
