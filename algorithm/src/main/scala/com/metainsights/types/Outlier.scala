package com.metainsights.types

import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.hadoop.shaded.org.apache.commons.math3.distribution.NormalDistribution

object Outlier extends PatternType {
  override val name: String = "Outlier"

  override def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult = {
    if (!satisfiesTemporalDimensions(dataScope)) {
      return nonEvaluatingResult()
    }
    if (results.length < 3) {
      return nonEvaluatingResult()
    }
    val sortedValues = results.sortBy(_._1.asInstanceOf[Double])

    val yValues = sortedValues.map(_._2)
    val stats = new DescriptiveStatistics(yValues)
    val mean = stats.getMean
    val stdDev = stats.getStandardDeviation
    val normalDistributionObj = new NormalDistribution(0, 3 * mean)

    val outliers = sortedValues.filter { case (_, y) =>
      math.abs(y - mean) > 2.1 * stdDev
    }.map { case (x, y) =>
      val score = normalDistributionObj.cumulativeProbability(y)
      (x, score)
    }

    if (outliers.length == 0) {
      return nonEvaluatingResult()
    }

    val score = outliers.maxBy(_._2)._2
    val highlights = outliers.map(_._1)

    PatternTypeResult(this, evaluates = true, score = score, highlight = highlights)
  }

  override def sortResults(results: Array[(Any, Double)]): Array[(Any, Double)] = {
    results.sortBy(_._1.asInstanceOf[Double])
  }
}
