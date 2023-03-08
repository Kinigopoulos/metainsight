package com.metainsights

import com.metainsights.data.DataController
import com.metainsights.patterns.Result
import org.apache.log4j.{LogManager, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

object Main {

  private val APP_NAME = "MetaInsight"
  private lazy val logger: Logger = LogManager.getLogger(this.getClass)
  private var sc: SparkContext = _

  var dataName = ""
  var metadataName = ""


  def main(args: Array[String]): Unit = {
    logger.info("Starting...")

    dataName = args(0)
    metadataName = args(1)
    if (args.length >= 3) {
      ParametersData.fromJson(args(2))
    }

    val results = getResults
    val json = toJSON(results)

    println(ParametersData.resultsPath)

    val data = sc.parallelize(Seq(json))
    data.coalesce(1).saveAsTextFile(ParametersData.resultsPath)
  }

  def getResults: Array[Result] = {
    val sparkSession = SparkSession
      .builder()
      .appName(APP_NAME)
      .master("local[*]")
      .getOrCreate()
    sc = sparkSession.sparkContext

    val advancedSettings = ParametersData.advancedSettings
    val balancingParameters = advancedSettings.balancingParameters

    DataController.readData(sparkSession = sparkSession, sparkContext = sc, dataName = dataName, metadataName = metadataName)
    DataController.setAdvancedParameters(advancedSettings.patternTypes, advancedSettings.extenders)
    DataController.setBalancingParameters(balancingParameters.t, balancingParameters.r, balancingParameters.k, balancingParameters.g)

    DataController.findDataPatterns(ParametersData.desiredTime)

    DataController.getInsights
  }

  def toJSON(results: Array[Result]): String = {
    JsonHelper.getJSON(results)
  }
}