package com.metainsights.patterns

import org.apache.hadoop.shaded.org.apache.commons.math3.util.FastMath
import com.metainsights.data.{DataController, DataScope}
import com.metainsights.types.PatternType

import scala.collection.mutable.ArrayBuffer

case class MetaInsight(mainPatternType: PatternType) {

  private var dataPatternSize = 0
  private var set: Map[(PatternType, Any), ArrayBuffer[(DataScope, PatternTypeResult)]] = Map()
  private var exceptions: Map[(PatternType, Any), ArrayBuffer[(DataScope, PatternTypeResult)]] = Map()
  private var score: Double = 0

  def addDataPattern(dataScope: DataScope, patternTypeResults: Array[PatternTypeResult]): Unit = {
    dataPatternSize += 1
    if (patternTypeResults.length == 0) {
      exceptions += ((null, null) -> ArrayBuffer((dataScope, null)))
      return
    }

    val mainPatternTypeResult = patternTypeResults.find(patternTypeResult => patternTypeResult.patternType == mainPatternType)
    val patternTypeResult = mainPatternTypeResult.getOrElse(patternTypeResults(0))

    if (set.contains(patternTypeResult.patternType, patternTypeResult.highlight)) {
      set((patternTypeResult.patternType, patternTypeResult.highlight)) += ((dataScope, patternTypeResult))
      return
    }

    set += ((patternTypeResult.patternType, patternTypeResult.highlight) -> ArrayBuffer((dataScope, patternTypeResult)))
  }

  def excludeFromSetWithSmallerDensity(t: Double): Unit = {
    set = set.filter(c => {
      val isValid = (c._2.length.toDouble / dataPatternSize) >= t
      if (!isValid) {
        exceptions += (c._1 -> c._2)
      }
      isValid
    })
  }

  def setIsEmpty(): Boolean = {
    set.isEmpty
  }

  def setHasOnlyOneElement(): Boolean = {
    if (set.size == 1 && exceptions.isEmpty) {
      val arr = set.toArray
      return arr(0)._2.length == 1
    }
    false
  }

  private def categorizeExceptions(result: (PatternType, Any)): String = {
    if (result._1 == null) return "No Pattern"
    if (result._1 != mainPatternType) return "Other Pattern"
    "Highlight Change"
  }

  def getConciseness(t: Double, r: Double, k: Double, g: Double): Double = {
    val size = dataPatternSize.toDouble
    val A: Array[Double] = set.toArray
      .map(c => c._2.length / size)
    val B: Array[Double] = exceptions.toArray
      .groupBy(e => categorizeExceptions(e._1))
      .map(e => e._2.flatMap(array => array._2).length / size)
      .toArray

    var S: Double = 0
    A.foreach(a => {
      S -= a * FastMath.log(a)
    })
    if (B.length > 0) {
      B.foreach(b => {
        S -= r * b * FastMath.log(b)
      })
    } else {
      S += g
    }

    var maxS: Double = 0
    val kLimit = ((1 - t) * FastMath.E) / FastMath.pow(t, 1 / r)
    if (k < kLimit) {
      maxS = - FastMath.log(t) + (r * k * FastMath.pow(t, 1 / r) / FastMath.E) * FastMath.log(FastMath.E)
    } else {
      maxS = - t * FastMath.log(t) - r * (1 - t) * FastMath.log((1 - t) / k)
    }

    1 - (S / maxS)
  }

  def setScore(score: Double): Unit = {
    this.score = score
  }

  def positiveScore: Boolean = {
    this.score > 0
  }

  def toResult: Result = {
    val setTransformed = set.toArray.flatMap(c => {
      c._2.map(dataScope => (c._1._1.name, dataScope._1, DataController.getQueryResultSorted(dataScope._1, dataScope._2.patternType), dataScope._2))
    })
    val exceptionsTransformed = exceptions.toArray.flatMap(e => {
      e._2.map(dataScope => {
        val resultSet = if (dataScope._2 != null) DataController.getQueryResultSorted(dataScope._1, dataScope._2.patternType) else DataController.getQueryResult(dataScope._1)
        (categorizeExceptions(e._1), dataScope._1, resultSet, dataScope._2)
      })
    })
    val setsSize = setTransformed.length
    val exceptionsSize = exceptionsTransformed.length
    Result(setTransformed ++ exceptionsTransformed, score, setsSize, exceptionsSize)
  }

  override def toString: String = s"MetaInsight(${mainPatternType.name}, ${dataPatternSize}), ${score}"

}
