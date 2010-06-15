/*
 * Copyright 2005-2010 LAMP/EPFL
 */
// $Id$

package scala.tools.refactoring
package analysis

import scala.tools.nsc.ast.Trees
import scala.tools.refactoring.common.Selections

trait TreeAnalysis {
  
  self: Selections with Indexes with common.PimpedTrees /*really needed?*/ =>
  
  val global: scala.tools.nsc.interactive.Global
  
  def inboundLocalDependencies(selection: Selection, currentOwner: global.Symbol, index: IndexLookup) = {

    selection.selectedSymbols filter {
        _.ownerChain.contains(currentOwner)
    } filterNot {
      index.declaration(_).map(selection.contains) getOrElse false
    } sortBy(_.pos.start) distinct
  }
  
  def outboundLocalDependencies(selection: Selection, currentOwner: global.Symbol, index: IndexLookup) = {
        
    val declarationsInTheSelection = selection.selectedSymbols filter (s => index.declaration(s).map(selection.contains) getOrElse false)
    
    val occurencesOfSelectedDeclarations = declarationsInTheSelection flatMap (index.occurences)
    
    occurencesOfSelectedDeclarations filterNot (selection.contains) map (_.symbol) distinct
  }
}
