package com.metainsights.data

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions._

object AggregationFunctions {
  private val map: Map[String, String => Column] = Map(
    "SUM" -> ((name: String) => sum(name)),
    "AVG" -> ((name: String) => avg(name)),
    "COUNT" -> ((name: String) => count(name)),
    "MIN" -> ((name: String) => min(name)),
    "MAX" -> ((name: String) => max(name))
  )

  def get(function: String): String => Column = {
    map.getOrElse(function, sum)
  }
}
