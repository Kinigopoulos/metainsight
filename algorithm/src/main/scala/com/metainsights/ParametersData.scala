package com.metainsights

import com.google.gson.{JsonArray, JsonParser}
import com.metainsights.patterns._
import com.metainsights.types._

object ParametersData {
  var resultsPath = "results"
  var desiredTime = .0

  private val allExtenders: Array[Extender] = Array(
    SubspaceExtender,
    BreakdownExtender,
    MeasureExtender
  )

  val allPatternTypes: Array[PatternType] = Array(
    OutstandingFirst,
    OutstandingLast,
    Evenness,
    OutstandingTop2,
    OutstandingLast2,
    Trend,
    Outlier,
    Seasonality,
    ChangePoint,
    Unimodality,
    Attribution
  )

  private val allPatternTypeResultFunctions: Array[PatternTypeResultFunction] = Array(
    PatternTypeResultFunctionTop,
    PatternTypeResultFunctionThreshold,
    PatternTypeResultFunctionAll
  )

  var advancedSettings: AdvancedSettings = AdvancedSettings()

  case class AdvancedSettings(
                               balancingParameters: BalancingParameters = BalancingParameters(),
                               extenders: Array[Extender] = allExtenders,
                               patternTypes: Array[PatternType] = allPatternTypes,
                               patternTypeFunction: PatternTypeResultFunction = PatternTypeResultFunctionTop,
                               patternTypeFunctionValue: Double = 1.0
                             )

  case class BalancingParameters(t: Double = 0.3, r: Double = 1, k: Int = 3, g: Double = 0.1)

  private def toStringArray(array: JsonArray): Array[String] = {
    (0 until array.size).map(i => array.get(i).getAsString).toArray
  }

  def fromJson(jsonString: String): Unit = {

    try {
      val json = new JsonParser().parse(jsonString).getAsJsonObject

      val resultsPath = json.get("resultsPath").getAsString
      val desiredTime = json.get("desiredTime").getAsDouble

      val advancedSettings = json.get("advancedSettings").getAsJsonObject

      val balancingParameters = advancedSettings.get("balancingParameters").getAsJsonObject
      val t = balancingParameters.get("t").getAsDouble
      val r = balancingParameters.get("r").getAsDouble
      val k = balancingParameters.get("k").getAsInt
      val g = balancingParameters.get("g").getAsDouble
      val newBalancingParameters = BalancingParameters(t, r, k, g)

      val extendersJsonArray = advancedSettings.get("extenders").getAsJsonArray
      val extendersArray = toStringArray(extendersJsonArray)
      val patternTypesJsonArray = advancedSettings.get("patternTypes").getAsJsonArray
      val patternTypesArray = toStringArray(patternTypesJsonArray)

      val patternTypeResultFunctionString = advancedSettings.get("patternTypeResultFunction").getAsString
      val patternTypeResultFunctions = allPatternTypeResultFunctions.filter(patternTypeResultFunction => patternTypeResultFunction.name == patternTypeResultFunctionString)
      var patternTypeResultFunction: PatternTypeResultFunction = PatternTypeResultFunctionTop
      if (patternTypeResultFunctions.length > 0) {
        patternTypeResultFunction = patternTypeResultFunctions(0)
      }
      if (advancedSettings.has("patternTypeFunctionValue")) {
        patternTypeResultFunction.p = advancedSettings.get("patternTypeFunctionValue").getAsDouble
      }

      val extenders = allExtenders.filter(extender => extendersArray.contains(extender.name))
      val patternTypes = allPatternTypes.filter(patternType => patternTypesArray.contains(patternType.name))

      val newAdvancedSettings = AdvancedSettings(newBalancingParameters, extenders, patternTypes, patternTypeResultFunction)

      this.resultsPath = resultsPath
      this.desiredTime = desiredTime
      this.advancedSettings = newAdvancedSettings

    } catch {
      case e: Exception =>
        println("Problem with the JSON parameters... Reverting to default ones. Error:")
        println(e)
    }
  }

}
