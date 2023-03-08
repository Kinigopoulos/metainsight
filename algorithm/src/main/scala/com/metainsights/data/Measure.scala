package com.metainsights.data

import org.apache.spark.sql.Column

case class Measure(name: String, aggregationType: String, columnIndex: Int) {

  override def toString: String = s"Measure($name, $aggregationType)"

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Measure]

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: Measure => that.name == this.name && that.aggregationType == this.aggregationType
      case _ => false
    }
  }

  def getAggregationFunction: Column = {
    val function = AggregationFunctions.get(aggregationType)
    function(name)
  }

  override def hashCode(): Int = {
    7 * (7 + aggregationType.hashCode) + name.hashCode
  }

}
