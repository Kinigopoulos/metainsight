package com.metainsights.patterns

trait PatternTypeResultFunction {

  val name: String
  var p: Double = 1.0
  def get(patternTypeResults: Array[PatternTypeResult]): Array[PatternTypeResult]

}
