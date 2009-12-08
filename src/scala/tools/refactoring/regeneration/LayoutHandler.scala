package scala.tools.refactoring.regeneration

import scala.tools.refactoring.Tracing
import scala.collection.mutable.ListBuffer

trait LayoutHandler {
  self: Tracing =>
  
  def processRequisites(current: Fragment, layoutAfterCurrent: String, layoutBeforeNext: String, next: Fragment) = context("requisites") {
  
    trace("layout %s", layoutAfterCurrent + layoutBeforeNext)
    
    // check for overlapping layouts and requirements! => testSortWithJustOne
    def getRequisite(r: Requisite) = if(!(layoutAfterCurrent + layoutBeforeNext).contains(r.check)) r.write else ""
      
    def mapRequirements(rs: ListBuffer[Requisite]) = rs.map( getRequisite ) mkString ""

    using(layoutAfterCurrent + mapRequirements(current.requiredAfter) + layoutBeforeNext + mapRequirements(next.requiredBefore)) {
      trace("results in %s", _)
    }
  }
  
  def fixIndentation(layout: String, existingIndentation: Option[Tuple2[Int, Int]], isEndOfScope: Boolean, currentScopeIndentation: Int): String = context("fix indentation") {

    if(layout.contains('\n')) {
      
      def indentString(length: Int) = {
        layout.replaceAll("""(?ms)\n[\t ]*""", "\n" + (" " * length))
      }
      
      existingIndentation match {
        case Some((originalScopeIndentation, originalIndentation)) =>
          trace("this is a reused fragment")

            val newIndentation = currentScopeIndentation + (originalIndentation - originalScopeIndentation)
            
            trace("original indentation was %d, original scope indentation was %d", originalIndentation, originalScopeIndentation)
            trace("new scope's indentation is %d → indent to %d", currentScopeIndentation, newIndentation)
            
            if(newIndentation != originalIndentation) indentString(newIndentation) else layout
          
        case None =>
          trace("this is a new fragment")
        
          if(isEndOfScope) {
            trace("at the end of the scope, take scope's parent indentation %d", currentScopeIndentation)
            indentString(currentScopeIndentation)
          } else {
            trace("new scope's indentation is %d → indent to %d ", currentScopeIndentation, currentScopeIndentation + 2)
            indentString(currentScopeIndentation + 2)
          }
      }
    } else layout
  }

  def splitLayoutBetween(parts: Option[Triple[Fragment,List[Fragment],Fragment]]) = parts match {
    
    case Some((left, layoutFragments, right)) =>
      context("split layout") {
        val OpeningBrace = """(.*?\()(.*)""".r
        val ClosingBrace = """(?ms)(.*?)(\).*)""".r
        val Comma = """(.*?),\s*(.*)""".r
        val NewLine = """(?ms)(.*?\n)(.*)""".r
        
        // strip comments!
        val layout = layoutFragments mkString ""

        trace("splitting layout %s between %s and %s", layout, left, right)
        
        ((left, layout, right) match {
          case(_, OpeningBrace(l, r), _) => (l, r, "OpeningBrace")
          case(_, ClosingBrace(l, r), _) => (l, r, "ClosingBrace")
          case(_, NewLine(l, r)     , _) => (l, r, "NewLine")
          case(_, Comma(l, r),        _) => (l, r, "Comma")
          case(_, s                 , _) => (s, "","NoMatch")
        }) match {
          case(l, r, why) => 
            trace("layout splits into %s and %s", l, r)
            (l, r)
        }
      }
    case None => ("", "")
  }
}