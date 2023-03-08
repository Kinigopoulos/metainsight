package com.metainsights.types

import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.hadoop.shaded.org.apache.commons.math3.distribution.NormalDistribution

object ChangePoint extends PatternType {
  override val name: String = "Change Point"

  override def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult = {
    if (!satisfiesTemporalDimensions(dataScope)) {
      return nonEvaluatingResult()
    }
    if (results.length < 3) {
      return nonEvaluatingResult()
    }

    val sortedValues = results.sortBy(_._1.asInstanceOf[Double])

    if (sortedValues.length < 3) {
      return nonEvaluatingResult()
    }

    val meanObj = new Mean()

    val mean = meanObj.evaluate(sortedValues.map(_._2), 0, sortedValues.length)

    val normalDistributionObj = new NormalDistribution(0, mean)

    var bestResult = nonEvaluatingResult()
    for ((element, index) <- sortedValues.zipWithIndex) {
      if (index != 0 && index != sortedValues.length - 1) {
        val value = element._2
        val (left, right) = sortedValues.map(_._2).splitAt(index)
        val meanLeft = meanObj.evaluate(left, 0, left.length)
        val meanRight = meanObj.evaluate(right, 0, right.length)

        val minMean = Math.min(meanLeft, meanRight)
        val maxMean = Math.max(meanLeft, meanRight)
        val isChangePoint = minMean < value && maxMean > value
        if (isChangePoint) {
          val score = normalDistributionObj.probability(minMean, maxMean)
          if (bestResult.score < score) {
            bestResult = PatternTypeResult(this, evaluates = true, score = score, highlight = element._1)
          }
        }
      }
    }

    bestResult
  }

  override def sortResults(results: Array[(Any, Double)]): Array[(Any, Double)] = {
    results.sortBy(_._1.asInstanceOf[Double])
  }
}
