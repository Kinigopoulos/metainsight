package com.metainsights.types

import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult

object OutstandingTop2 extends PatternType with Outstand {
  override val name: String = "Outstanding Top 2"

  override def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult = {
    if (results.length < 3) {
      return nonEvaluatingResult()
    }
    val sortedResults = results.sortBy(_._2).reverse

    def filterIndex(index: Int): Array[(Any, Double)] = {
      sortedResults.zipWithIndex.filter { case (_, i) => i != index }.map(_._1)
    }

    val first = filterIndex(1)
    val second = filterIndex(0)

    val resultsFirst = OutstandingFirst.evaluate(dataScope, first)
    val resultsSecond = OutstandingFirst.evaluate(dataScope, second)

    if (!resultsFirst.evaluates || !resultsSecond.evaluates) {
      return nonEvaluatingResult()
    }

    val score = (resultsFirst.score + resultsSecond.score) / 2
    val highlight = Array(resultsFirst.highlight, resultsSecond.highlight)

    PatternTypeResult(this, evaluates = true, score = score, highlight = highlight)
  }

  override def sortResults(results: Array[(Any, Double)]): Array[(Any, Double)] = {
    results.sortBy(_._2).reverse
  }
}
