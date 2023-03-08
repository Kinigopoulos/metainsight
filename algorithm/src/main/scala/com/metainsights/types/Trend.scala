package com.metainsights.types

import org.apache.commons.math3.distribution.LogisticDistribution
import org.apache.hadoop.shaded.org.apache.commons.math3.stat.regression.SimpleRegression
import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult

object Trend extends PatternType {
  override val name: String = "Trend"

  val distribution: LogisticDistribution = new LogisticDistribution(0.2, 2)

  override def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult = {
    if (!satisfiesTemporalDimensions(dataScope)) {
      return nonEvaluatingResult()
    }

    val sortedValues = results.sortBy(_._1.asInstanceOf[Double])
    if (sortedValues.length <= 1 || sortedValues(0)._2 == sortedValues.last._2) {
      return nonEvaluatingResult()
    }

    val regression: SimpleRegression = new SimpleRegression()
    results.foreach(result => {
      regression.addData(result._1.asInstanceOf[Double], result._2)
    })

    val slope = regression.getSlope
    val rSquare = regression.getRSquare
    if (slope == 0 || rSquare == 0) {
      return nonEvaluatingResult()
    }

    val prob = distribution.cumulativeProbability(Math.abs(slope))
    val score = prob * rSquare
    val highlight = if (slope > .0) "increasing" else "decreasing"

    PatternTypeResult(this, evaluates = true, score = score, highlight = highlight)
  }

  override def sortResults(results: Array[(Any, Double)]): Array[(Any, Double)] = {
    results.sortBy(_._1.asInstanceOf[Double])
  }
}
