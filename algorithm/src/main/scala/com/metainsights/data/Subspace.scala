package com.metainsights.data

import java.util

case class Subspace(array: Array[Filter]) {

  override def toString: String = {
    if (array.isEmpty) {
      return s"*"
    }
    s"${array.mkString(", ")}"
  }

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Subspace]

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: Subspace => that.array.length == this.array.length && that.array.forall(filter => this.array.contains(filter))
      case _ => false
    }
  }

  override def hashCode(): Int = {
    util.Arrays.hashCode(array.asInstanceOf[Array[Object]])
  }

}
