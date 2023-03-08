package com.metainsights.patterns

object PatternTypeResultFunctionTop extends PatternTypeResultFunction {

  override val name: String = "PatternTypeResultFunctionTop"

  p = 3.0

  override def get(patternTypeResults: Array[PatternTypeResult]): Array[PatternTypeResult] = {
    val sortedByScore = patternTypeResults.sortBy(_.score).reverse
    val numberOfElements = if (patternTypeResults.length >= p) p.toInt else patternTypeResults.length
    sortedByScore.take(numberOfElements)
  }
}
