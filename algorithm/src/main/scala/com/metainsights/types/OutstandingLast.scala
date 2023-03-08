package com.metainsights.types

import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.fitting.WeightedObservedPoints
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import org.apache.commons.math3.util.FastMath
import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult

object OutstandingLast extends PatternType with Outstand {
  override val name: String = "Outstanding Last"

  override def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult = {
    if (results.length < 2) {
      return nonEvaluatingResult()
    }
    val sortedResults = results.sortBy(_._2).reverse

    val min = sortedResults.last
    val sortedValues = sortedResults.dropRight(1).zipWithIndex
    if (sortedValues.length == 0 || sortedValues(0)._1._2 == min._2) {
      return this.nonEvaluatingResult()
    }

    val fitter = new fitter()
    val points = new WeightedObservedPoints()
    sortedValues.foreach { case (value, index) => points.add(index + 1, value._2) }

    val coefficients = fitter.fit(points.toList)
    val intercept = coefficients(0)
    val slope = coefficients(1)

    val residuals: Array[Double] = sortedValues.map { case (value, index) =>
      val predictedValue = intercept * FastMath.pow(index + 1, slope)
      predictedValue - value._2
    }

    val xMaxErr = - min._2 + intercept * FastMath.pow(sortedValues.length + 1, slope)

    val meanObj = new Mean()
    val mean = meanObj.evaluate(residuals, 0, residuals.length)

    val standardDeviationObj = new StandardDeviation()
    val standardDeviation = standardDeviationObj.evaluate(residuals, mean)
    if (standardDeviation == 0) {
      return this.nonEvaluatingResult()
    }

    val normalDistribution = new NormalDistribution(mean, standardDeviation * 7)
    val probability = normalDistribution.cumulativeProbability(xMaxErr)

    PatternTypeResult(patternType = this, evaluates = true, score = probability, highlight = min._1)
  }

  override def sortResults(results: Array[(Any, Double)]): Array[(Any, Double)] = {
    results.sortBy(_._2).reverse
  }
}
