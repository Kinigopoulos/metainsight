package com.metainsights.types

import org.apache.commons.math3.stat.descriptive.moment.{Mean, StandardDeviation}
import org.apache.commons.math3.util.FastMath
import org.apache.hadoop.shaded.org.apache.commons.math3.distribution.NormalDistribution
import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult

object Evenness extends PatternType {
  override val name: String = "Evenness"

  override def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult = {
    if (results.length <= 3) {
      return nonEvaluatingResult()
    }

    var values = results.map(_._2)
    val meanObj = new Mean()
    var mean = meanObj.evaluate(values, 0, values.length)
    if (mean == 0) {
      mean += 1
      values = values.map(value => value + 1)
    }

    val meanDifference = values.map(value => FastMath.pow(value - mean, 2))
    val normalDistributionObj = new NormalDistribution(0, mean)
    val probabilities = meanDifference.map(value => {
      1 - normalDistributionObj.probability(-value, value)
    })

    val probability = meanObj.evaluate(probabilities, 0, probabilities.length)

    PatternTypeResult(patternType = this, evaluates = true, score = probability, highlight = "Even")
  }
}
