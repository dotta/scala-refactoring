package scala.tools.refactor.tests.utils

import scala.tools.refactor.Compiler
import scala.tools.refactor.transform.Transform
import scala.tools.refactor.UnknownPosition
import scala.tools.nsc.util.Position
import scala.tools.nsc.util.RangePosition
import scala.tools.nsc.ast.parser.Tokens
import scala.tools.nsc.symtab.Flags

trait TestTransform extends Transform {
  
  self: scala.tools.refactor.Compiler =>
  import compiler._
  
  def insertNewMethod = new Transformer {
    override def transform(tree: Tree): Tree = {
      super.transform(tree) match {
        
        case tpl @ Template(parents, self, body) => 
          
            val typ: TypeTree = body(1) match {
              case tree: DefDef => tree.tpt.asInstanceOf[TypeTree]
            }
            
            val v = ValDef(NoMods, newTermName("sample"), TypeTree(typ.tpe), EmptyTree)

            val block = Block( Literal(555) :: v :: Nil, EmptyTree)
                      
            val d = cleanTree {
              DefDef(Modifiers(Flags.METHOD), newTermName("method"), Nil, Nil, TypeTree(typ.tpe), block)
            }
            
            new Template(parents, self, d :: body).copyAttrs(tree)
          
        case x => x
      }
    }
  }
  
  def copyLastMethod = new Transformer {
    override def transform(tree: Tree): Tree = {
      super.transform(tree) match {
        
        case tpl @ Template(parents, self, body) => 
          
            new Template(parents, self, body.last :: body).copyAttrs(tree)
          
        case x => x
      }
    }
  }
  
  def newMethodFromExistingBody = new Transformer {
    override def transform(tree: Tree): Tree = {
      super.transform(tree) match {
        
        case tpl @ Template(parents, self, body) => 
          
          val typ: TypeTree = body(1) match {
            case tree: DefDef => tree.tpt.asInstanceOf[TypeTree]
          }
          
          val v = ValDef(NoMods, newTermName("arg"), TypeTree(typ.tpe), EmptyTree)
          
          val rhs = body.last match {
            case tree: ValOrDefDef => tree.rhs
          }

          val d = cleanTree {
            DefDef(Modifiers(Flags.METHOD), newTermName("newMethod"), Nil, (v :: Nil) :: Nil, TypeTree(typ.tpe), rhs)
          }
          
          new Template(parents, self, d :: body).copyAttrs(tree)
        case x => x
      }
    }
  }
}
