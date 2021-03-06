// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstNode, CommandBlock, CompilationOperand,
  Expression, FrontEndInterface, FrontEndProcedure, Instruction,
  ProcedureDefinition, ProcedureSyntax, ReporterApp, ReporterBlock,
  SourceRewriter, Statement, Statements, StructureResults,
  Token, TokenizerInterface, TokenType }

import scala.util.matching.Regex

class AstRewriter(val tokenizer: TokenizerInterface, op: CompilationOperand)
  extends SourceRewriter
  with NetLogoParser {
  type WhiteSpaceMap = Map[AstPath, String]

  def preserveBody(structureResults: StructureResults, header: String, procedures: String, footer: String): String = header + procedures + footer

  def remove(dropCommand: String): String = {
    rewrite(new RemovalVisitor(dropCommand), preserveBody _)
  }

  def addCommand(addCommand: (String, String)): String = {
    rewrite(new AddVisitor(addCommand), preserveBody _)
  }

  def replaceCommand(replaceCommand: (String, String)): String = {
    rewrite(new ReplaceVisitor(replaceCommand), preserveBody _)
  }

  def replaceReporter(replaceReporter: (String, String)): String = {
    rewrite(new ReplaceReporterVisitor(replaceReporter), preserveBody _)
  }

  def replaceToken(original: String, replacement:String): String = {
    val source = op.sources("")
    val tokens = tokenizer.tokenizeString(source)
    val buf = new StringBuilder(source)
    var offset = 0
    for (token <- tokens if token.text.equalsIgnoreCase(original)) {
      buf.delete(token.start + offset, token.end + offset)
      buf.insert(token.start + offset, replacement)
      offset += replacement.length - token.text.length
    }
    buf.toString
  }

  def addExtension(newExtension: String): String = {
    rewrite(NoopFolder, declarationReplace("extensions", extensionsRegex, _.extensions.map(_.text), newExtension))
  }

  def addGlobal(newGlobal: String): String = {
    rewrite(NoopFolder, declarationReplace("globals", globalsRegex, _.program.userGlobals.map(_.toLowerCase), newGlobal))
  }

  private val extensionsRegex = new Regex("(?i)(?m)extensions\\s+\\[[^]]*\\]")
  private val globalsRegex = new Regex("(?i)(?m)globals\\s+\\[[^]]*\\]")

  private def declarationReplace(
    declKeyword: String,
    declRegex: Regex,
    declItems: StructureResults => Seq[String],
    addedItem: String)(
    res: StructureResults, headers: String, procedures: String, footer: String): String = {
    val newDecl =
      declKeyword + " " + (declItems(res) :+ addedItem).distinct.mkString("[", " ", "]")
    val modifiedHeaders = declRegex
      .findFirstMatchIn(headers)
      .map(m => headers.take(m.start) + newDecl + headers.drop(m.end))
      .getOrElse(newDecl+ "\n" + headers)
    modifiedHeaders + procedures + footer
  }

  def rewrite(
    visitor: PositionalAstFolder[Map[AstPath, Formatter.Operation]],
    wholeFile: (StructureResults, String, String, String) => String): String = {

    val (procs, structureResults) = basicParse(op)

    def getSource(filename: String): String =
      op.sources.get(filename).orElse(IncludeFile(op.compilationEnvironment, filename).map(_._2))
        .getOrElse(throw new Exception("Unable to find file: " + filename))

    val (wsMap, fileHeaders, fileFooters) = trackWhitespace(getSource _, procs)

    val operations = procs.foldLeft(Map[AstPath, Formatter.Operation]()) {
      case (dels, proc) => visitor.visitProcedureDefinition(proc)(dels)
    }

    val headers = fileHeaders.getOrElse("", "")
    val footer = fileFooters.getOrElse("", "")

    val eolWhitespace = new Regex("\\s+$")
    val rewritten =
      wholeFile(structureResults, headers, format(operations, wsMap, procs), footer)
    rewritten.lines.map(eolWhitespace.replaceAllIn(_, "")).mkString("\n")
  }

  def trackWhitespace(getSource: String => String, procs: Iterable[ProcedureDefinition]): (Map[AstPath, WhiteSpace], Map[String, String], Map[String, String]) = {
    val ws = new WhiteSpace.Tracker(getSource)
    var fileHeaders: Map[String, String] = Map()
    var fileFooters: Map[String, String] = Map()
    var procedurePositions: Map[String, Map[String, ProcedureSyntax]] = Map()
    def procedurePosition(file: String, procedureName: String): ProcedureSyntax = {
      if (procedurePositions.isDefinedAt(file))
        procedurePositions(file)(procedureName)
      else {
        val newPositions =
          findProcedurePositions(getSource(file), Some(op.containingProgram.dialect)).map {
            case (k, v) => k.toUpperCase -> v
          }
        procedurePositions = procedurePositions + (file -> newPositions)
        newPositions(procedureName)
      }
    }

    def addTrailingWhitespace(ctx: WhiteSpace.Context): Unit = {
      ctx.lastPosition.foreach {
        case (lastFile, _, lastIndex) =>
          fileFooters = (fileFooters + (lastFile -> getSource(lastFile).drop(lastIndex)))
      }
    }

    val whiteSpaces =
      procs.foldLeft(
        (Map[AstPath, WhiteSpace](),
          WhiteSpace.Context(Map(), None))) {
      case ((wsMap, ctx), proc) =>
        val procSyntax = procedurePosition(proc.file, proc.procedure.name)
        val newContext =
          if (ctx.lastFile.contains(proc.file)) {
            addTrailingWhitespace(ctx)
            WhiteSpace.Context(Map(), ctx.lastPosition)
          } else {
            val procStart = procSyntax.declarationKeyword.start
            fileHeaders = (fileHeaders + (proc.file -> getSource(proc.file).slice(0, procStart)))
            WhiteSpace.Context(Map(), lastPosition = Some((proc.file, AstPath(), procStart)))
          }
        val r = ws.visitProcedureDefinition(proc)(newContext)
        (wsMap ++ r.astWsMap, r)
    }
    addTrailingWhitespace(whiteSpaces._2)
    (whiteSpaces._1, fileHeaders, fileFooters)
  }

  def format(
    operations: Map[AstPath, Formatter.Operation],
    wsMap: Map[AstPath, WhiteSpace],
    procs: Iterable[ProcedureDefinition]): String = {
    val formatter = new Formatter
    val res = procs.filter(p => op.sources.contains(p.procedure.filename)).foldLeft(Formatter.Context("", operations)) {
      case (acc, proc) =>
        formatter.visitProcedureDefinition(proc)(
          acc.copy(instructionToString = Formatter.instructionString _, wsMap = wsMap))
    }
    res.text
  }
}
