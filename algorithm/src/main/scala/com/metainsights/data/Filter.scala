package com.metainsights.data

case class Filter(dimension: Dimension, value: Any) {

  override def toString: String = s"Filter(${dimension.name}: $value)"

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Filter]

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: Filter => that.dimension == this.dimension && that.value == this.value
      case _ => false
    }
  }

  override def hashCode(): Int = {
    31 * (31 + dimension.name.hashCode) + value.hashCode
  }

  def createFilter(value: Any): Filter = {
    Filter(this.dimension, value)
  }

}
