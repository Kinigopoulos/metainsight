package com.metainsights.types

import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult

object Seasonality extends PatternType {

  override val name: String = "Seasonality"

  override def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult = {
    if (!satisfiesTemporalDimensions(dataScope)) {
      return nonEvaluatingResult()
    }

    val sortedValues = results.sortBy(_._1.asInstanceOf[Double])
    if (sortedValues.length < 3) {
      return nonEvaluatingResult()
    }

    val values = sortedValues.map(_._2)
    if (isIncreasing(values) || isDecreasing(values)) {
      return nonEvaluatingResult()
    }

    val trend = sortedValues.sliding(2).map { case Array(prev, curr) =>
      if (curr._2 > prev._2) "increasing"
      else if (curr._2 == prev._2) "same"
      else "decreasing"
    }.toArray

    def checkIfRepeats(index: Int, step: Int): Boolean = {
      var currentIndex = index
      val state = trend(currentIndex)
      while (currentIndex < trend.length) {
        if (state != trend(currentIndex)) {
          return false
        }
        currentIndex += step
      }
      true
    }

    val seasons = (2 to trend.length / 2).toArray
    seasons.foreach(season => {
      val indexes = (0 until season).toArray
      if (indexes.forall(index => checkIfRepeats(index, season))) {
        return PatternTypeResult(this, evaluates = true, score = 1.0, highlight = season)
      }
    })

    nonEvaluatingResult()
  }

  override def sortResults(results: Array[(Any, Double)]): Array[(Any, Double)] = {
    results.sortBy(_._1.asInstanceOf[Double])
  }
}
