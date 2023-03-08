package com.metainsights.types

import com.metainsights.data.DataScope
import com.metainsights.patterns.PatternTypeResult

trait PatternType {
  val name: String
  def evaluate(dataScope: DataScope, results: Array[(Any, Double)]): PatternTypeResult

  def satisfiesTemporalDimensions(dataScope: DataScope): Boolean = {
    dataScope.dimension.categoryType == "Temporal"
  }

  def nonEvaluatingResult(): PatternTypeResult = {
    PatternTypeResult(patternType = this, evaluates = false, score = 0, highlight = null)
  }

  def sortResults(results: Array[(Any, Double)]): Array[(Any, Double)] = {
    results
  }

  override def toString: String = s"${name}"

  def isIncreasing(array: Array[Double]): Boolean = {
    if (array.length < 2) true
    else {
      var i = 1
      while (i < array.length) {
        if (array(i) < array(i - 1)) {
          return false
        }
        i += 1
      }
      true
    }
  }

  def isDecreasing(array: Array[Double]): Boolean = {
    if (array.length < 2) true
    else {
      var i = 1
      while (i < array.length) {
        if (array(i) > array(i - 1)) {
          return false
        }
        i += 1
      }
      true
    }
  }
}
