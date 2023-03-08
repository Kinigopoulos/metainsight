package com.metainsights.patterns

object PatternTypeResultFunctionAll extends PatternTypeResultFunction {

  override val name: String = "PatternTypeResultFunctionAll"

  override def get(patternTypeResults: Array[PatternTypeResult]): Array[PatternTypeResult] = {
    patternTypeResults.sortBy(_.score).reverse
  }

}
