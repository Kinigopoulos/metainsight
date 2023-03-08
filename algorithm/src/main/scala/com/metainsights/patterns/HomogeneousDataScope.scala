package com.metainsights.patterns

import com.metainsights.data.DataScope

import java.util

case class HomogeneousDataScope(array: Array[DataScope]) {

  override def toString: String = s"HomogeneousDataScope(${array.mkString("Array(", ", ", ")")})"

  override def canEqual(that: Any): Boolean = that.isInstanceOf[HomogeneousDataScope]

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: HomogeneousDataScope => that.array.length == this.array.length && that.array.forall(dataScope => this.array.contains(dataScope))
      case _ => false
    }
  }

  override def hashCode(): Int = {
    util.Arrays.hashCode(array.asInstanceOf[Array[Object]])
  }
}
