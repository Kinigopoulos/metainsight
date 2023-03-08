package com.metainsights.patterns

object PatternTypeResultFunctionThreshold extends PatternTypeResultFunction {

  override val name: String = "PatternTypeResultFunctionThreshold"

  p = 3.0

  override def get(patternTypeResults: Array[PatternTypeResult]): Array[PatternTypeResult] = {
    val sortedByScore = patternTypeResults.sortBy(_.score).reverse
    sortedByScore.filter(patternTypeResult => patternTypeResult.score >= p)
  }

}
