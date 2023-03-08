package com.metainsights.data

case class DataScope(subspace: Subspace, dimension: Dimension, measure: Measure) {

  override def toString: String = s"DataScope({$subspace}, $dimension, $measure)"

  override def canEqual(that: Any): Boolean = that.isInstanceOf[DataScope]

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: DataScope => that.subspace == this.subspace && that.dimension == this.dimension && that.measure == this.measure
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + subspace.hashCode()
    result = prime * result + dimension.hashCode()
    result = prime * result + measure.hashCode()
    result
  }

  def fromNewSubspace(subspace: Subspace): DataScope = {
    DataScope(subspace, dimension, measure)
  }

  def fromNewDimension(dimension: Dimension): DataScope = {
    DataScope(subspace, dimension, measure)
  }

  def fromNewMeasure(measure: Measure): DataScope = {
    DataScope(subspace, dimension, measure)
  }
}
