package com.metainsights.data

case class Dimension(name: String, categoryType: String, columnIndex: Int) {

  override def toString: String = s"Dimension($name, $categoryType)"

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Dimension]

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: Dimension => that.name == this.name
      case _ => false
    }
  }

  override def hashCode(): Int = {
    name.hashCode
  }

}
