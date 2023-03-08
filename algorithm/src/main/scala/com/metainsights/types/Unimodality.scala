package com.metainsights.types

import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.hadoop.shaded.org.apache.commons.math3.distribution.NormalDistribution

object Unimodality extends PatternType {

  override val name: String = "Unimodality"

  override def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult = {
    if (!satisfiesTemporalDimensions(dataScope)) {
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

    val maxElement = sortedValues.maxBy(_._2)
    if (maxElement != sortedValues(0) && maxElement != sortedValues.last) {
      val index = sortedValues.indexOf(maxElement)
      val (left, right) = sortedValues.map(_._2).splitAt(index)
      if (isIncreasing(left) && isDecreasing(right)) {
        val meanLeft = meanObj.evaluate(left, 0, left.length)
        val meanRight = meanObj.evaluate(right, 0, right.length)
        val score = (normalDistributionObj.cumulativeProbability(meanLeft) + normalDistributionObj.cumulativeProbability(meanRight)) / 2
        bestResult = PatternTypeResult(this, evaluates = true, score = score, highlight = maxElement._1)
      }
    }

    val minElement = sortedValues.minBy(_._2)
    if (minElement != sortedValues(0) && minElement != sortedValues.last) {
      val index = sortedValues.indexOf(minElement)
      val (left, right) = sortedValues.map(_._2).splitAt(index)
      if (isDecreasing(left) && isIncreasing(right)) {
        val meanLeft = meanObj.evaluate(left, 0, left.length)
        val meanRight = meanObj.evaluate(right, 0, right.length)
        val score = (normalDistributionObj.cumulativeProbability(meanLeft) + normalDistributionObj.cumulativeProbability(meanRight)) / 2
        bestResult = PatternTypeResult(this, evaluates = true, score = score, highlight = minElement._1)
      }
    }

    bestResult
  }

  override def sortResults(results: Array[(Any, Double)]): Array[(Any, Double)] = {
    results.sortBy(_._1.asInstanceOf[Double])
  }
}
