package x7c1.wheat.macros.fragment

import java.io.Serializable

import android.os.Bundle
import android.support.v4.app.Fragment

import scala.language.experimental.macros
import scala.reflect.macros.blackbox


object FragmentFactory {
  def create[F <: TypedFragment[_]]: FragmentFactory[F] = new FragmentFactory[F]
}

class FragmentFactory[F <: TypedFragment[_]]{
  def by[ARG](arg: ARG): F = macro FragmentFactoryImpl.create[F]
}

trait TypedFragment[ARG] extends Fragment {
  def getTypedArguments: ARG = macro TypedFragmentImpl.getTypedArguments[ARG]
}

private object FragmentFactoryImpl {
  def create[F: c.WeakTypeTag](c: blackbox.Context)(arg: c.Tree): c.Tree = {
    val factory = new BundleBuilderFactory {
      override val context: c.type = c
      override val fragmentType = c.weakTypeOf[F]
      override val argTree = arg
    }
    val tree = factory.buildBundle
//    println(c.universe.showCode(tree))
    tree
  }
}

private object TypedFragmentImpl {
  def getTypedArguments[A: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    val factory = new BundleExpanderFactory {
      override val context: c.type = c
      override val objectType = c.weakTypeOf[A]
    }
    val tree = factory.newInstance
//    println(c.universe.showCode(tree))
    tree
  }
}

private trait BundleBuilderFactory {
  val context: blackbox.Context

  import context.universe._

  val fragmentType: Type
  val argTree: Tree

  def buildBundle: Tree = {
    val Seq(arg, bundle, fragment) = createTermNames("arg", "bundle", "fragment")
    val assign = argTree.tpe.members.
      filter(_.isConstructor).map(_.asMethod).
      filter(_.paramLists exists (_.nonEmpty)).
      flatMap(_.paramLists flatMap { _ map toAssign(bundle, arg) })

    q"""
      val $arg = $argTree
      val $bundle = new ${typeOf[Bundle]}
      val $fragment = new $fragmentType

      ..$assign
      $fragment.setArguments($bundle)
      $fragment
    """
  }
  def toAssign(bundle: TermName, arg: TermName)(param: Symbol) = {
    val name = param.name.toString
    val tree = param.typeSignatureIn(argTree.tpe) match {
      case x if x <:< typeOf[Serializable] =>
        q"""$bundle.putSerializable($name, $arg.${TermName(name)})"""
      case x =>
        throw new IllegalArgumentException(s"unsupported type : $name:$x")
    }
    //    println(tree)
    tree
  }
  def createTermNames(names: String*): Seq[TermName] = {
    names map (x => TermName(context freshName x))
  }
}

private trait BundleExpanderFactory {
  val context: blackbox.Context
  import context.universe._
  val objectType: Type

  def newInstance: Tree = {
    val bundle = TermName(context freshName "bundle")
    val assign = objectType.members.
      filter(_.isConstructor).
      map(_.asMethod).
      filter(_.paramLists exists (_.nonEmpty)).
      flatMap(_.paramLists map { _ map toAssign(bundle) })

    val instantiate = assign.toSeq match {
      case paramList +: paramLists =>
        val first = q"new $objectType(..$paramList)"
        paramLists.foldLeft(first) { case (tree, params) => q"$tree(..$params)" }
      case _ =>
        q"new $objectType()"
    }
    q"""
      val $bundle = getArguments
      $instantiate
      """
  }
  def toAssign(bundle: TermName)(param: Symbol) ={
    val name = param.name.toString
    val tree = param.typeSignatureIn(objectType) match {
      case x if x <:< typeOf[Serializable] =>
        q"""${TermName(name)} = $bundle.getSerializable($name).asInstanceOf[$x]"""
      case x =>
        throw new IllegalArgumentException(s"unsupported type : $name:$x")
    }
//    println(tree)
    tree
  }
}
