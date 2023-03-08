package com.metainsights.types
import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult
import org.apache.hadoop.shaded.org.apache.commons.math3.distribution.NormalDistribution

object Attribution extends PatternType {
  override val name: String = "Attribution"

  override def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult = {

    if (results.length < 2) {
      return nonEvaluatingResult()
    }
    if (results.exists(_._2 < 0)) {
      return nonEvaluatingResult()
    }

    val values = results.map(_._2)
    val sum = values.sum
    if (sum == 0) {
      return nonEvaluatingResult()
    }
    val maxValue = values.max

    val percent = maxValue / sum
    if (percent < 0.5) {
      return nonEvaluatingResult()
    }

    val normalDistributionObj = new NormalDistribution(0, 1)

    val probability = normalDistributionObj.cumulativeProbability(percent)
    val highlight = results.filter(_._2 == maxValue)(0)
    PatternTypeResult(this, evaluates = true, score = probability, highlight = highlight._1)
  }
}
