package com.metainsights.data

import com.metainsights.ParametersData
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import com.metainsights.patterns.{Extender, ExtenderController, HomogeneousDataScope, MetaInsight, PatternTypeResult, Result}
import com.metainsights.types.PatternType

import scala.collection.mutable.ArrayBuffer

object DataController {

  private var sparkSession: SparkSession = _
  private var sparkContext: SparkContext = _
  private var metadataDF: DataFrame = _
  private var dataDF: DataFrame = _

  private var patternTypes: Array[PatternType] = _
  private var extenders: Array[Extender] = _

  private var t: Double = _
  private var r: Double = _
  private var k: Int = _
  private var g: Double = _

  private var homogeneousDataScopesSet: Set[(HomogeneousDataScope, PatternType)] = Set[(HomogeneousDataScope, PatternType)]()
  private var patternTypeResultsMap: Map[String, Array[PatternTypeResult]] = Map[String, Array[PatternTypeResult]]()
  private var impactMap: Map[Measure, Double] = Map[Measure, Double]()

  private val superSubspace: Subspace = Subspace(Array())
  private val metaInsights: ArrayBuffer[MetaInsight] = ArrayBuffer()

  class dimensionImpactTuple(val dimension: Dimension, val value: (Any, Long)) {}

  def readData(sparkSession: SparkSession, sparkContext: SparkContext, dataName: String, metadataName: String): Unit = {
    this.sparkSession = sparkSession
    this.sparkContext = sparkContext
    metadataDF = sparkSession.read.options(Map("header" -> "true"))
      .csv(metadataName)
      .persist(StorageLevel.MEMORY_AND_DISK)
    dataDF = sparkSession.read.options(Map("inferSchema" -> "true", "header" -> "true"))
      .csv(dataName)
      .persist(StorageLevel.MEMORY_AND_DISK)

    def castDimensionToDouble(name: String): Unit = dataDF = dataDF.withColumn(name, col(name).cast("double"))

    getTemporalDimensions.foreach(d => castDimensionToDouble(d.name))
    getMeasures.foreach(d => castDimensionToDouble(d.name))

    println("Data loaded...")
  }

  def setAdvancedParameters(patternTypes: Array[PatternType], extenders: Array[Extender]): Unit = {
    this.patternTypes = patternTypes
    this.extenders = extenders
  }

  def setBalancingParameters(t: Double, r: Double, k: Int, g: Double): Unit = {
    this.t = t
    this.r = r
    this.k = k
    this.g = g
  }

  /**
   * Constructs the dimensions array
   *
   * @return an array of tuples. 1st value is a string with the dimension name, 2nd it's category type (Categorical or Temporal) and 3rd it's index
   */
  def getDimensions: Array[Dimension] = {
    val dimensionsDF = metadataDF.filter(row => row.getAs[String]("CategoryType") != "Numerical")
    val dimensions = dimensionsDF.select("Name", "CategoryType")
      .collect
      .zipWithIndex
      .map { case (row, index) => Dimension(row(0).asInstanceOf[String], row(1).asInstanceOf[String], index) }
    dimensions
  }

  private def getTemporalDimensions: Array[Dimension] = {
    this.getDimensions.filter(row => row.categoryType == "Temporal")
  }

  /**
   * Constructs the measures array
   *
   * @return an array of tuples. 1st value is a string with the measure name, 2nd it's aggregation function and 3rd it's index
   */
  private def getMeasures: Array[Measure] = {
    val measuresDF = metadataDF.filter(row => row.getAs[String]("CategoryType") == "Numerical")
    val measures = measuresDF.select("Name", "Aggregation")
      .collect
      .zipWithIndex
      .map { case (row, index) => Measure(row(0).asInstanceOf[String], row(1).asInstanceOf[String], index) }
    measures
  }

  /**
   * @param dimension the dimension to take the values from
   * @return all the distinct values the dimension has
   */
  def getValuesFromDimension(dimension: String): Array[Any] = {
    dataDF.select(dimension)
      .distinct()
      .collect()
      .map(row => row(0))
  }


  private def filterRows(subspace: Subspace): DataFrame = {
    val filters = subspace.array
    var query = dataDF
    filters.foreach(filter => {
      query = query.filter(row => row.getAs(filter.dimension.name) == filter.value)
    })
    query
  }

  private def getImpact(dataScope: DataScope): Double = {
    if (dataScope.subspace == superSubspace && impactMap.contains(dataScope.measure)) {
      return impactMap.getOrElse(dataScope.measure, 0)
    }
    val query = filterRows(dataScope.subspace).select(dataScope.measure.name)
    query.agg(
      sum(dataScope.measure.name).cast("double")
    )
    val impact = query.first().getDouble(0)
    impactMap += (dataScope.measure -> impact)
    impact
  }

  def getSubspaces(minDepth: Int, maxDepth: Int): Array[Subspace] = {
    val superSubspace = Subspace(this.superSubspace.array.clone())
    val dimensions = getDimensions

    var addSuperSubspace = false
    var min = minDepth
    if (min <= 0) {
      addSuperSubspace = true
      min = 1
    } else if (min > dimensions.length) {
      return Array[Subspace]()
    }
    var max = maxDepth
    if (max > dimensions.length) max = dimensions.length
    val depths = (min to max).toArray

    var dimensionValueToImpactMap = Map[Dimension, Array[(Any, Long)]]()
    dimensions.foreach(dimension => {
      val query = dataDF.groupBy(dimension.name).count()
      val results = query.collect().map(value => (value(0), value(1).asInstanceOf[Long]))
      dimensionValueToImpactMap += (dimension -> results)
    })

    val subspaces = sparkContext.parallelize(depths)
      .flatMap(depth => dimensions.combinations(depth))
      .flatMap(subspace => {


        def getCombinations(arrays: Array[Array[dimensionImpactTuple]]): Array[Array[dimensionImpactTuple]] = {
          if (arrays.length == 1) return arrays(0).map(Array(_))
          val result = for {
            x <- arrays(0)
            y <- getCombinations(arrays.tail)
          } yield Array(x) ++ y
          result
        }

        val tuples = subspace.map(dimension => {
          dimensionValueToImpactMap(dimension).map(impactArray => new dimensionImpactTuple(dimension, impactArray))
        })
        val combinations = getCombinations(tuples)
        combinations
      })
      .map(combination => {
        val filters = combination.map(tuple => Filter(tuple.dimension, tuple.value._1))
        val estimatedImpact = combination.map(_.value._2).min
        (estimatedImpact, Subspace(filters))
      })
      .sortByKey(false)
      .map(_._2)
      .collect()

    if (addSuperSubspace) {
      return Array(superSubspace) ++ subspaces
    }
    subspaces
  }


  private def query(dataScope: DataScope): Array[Row] = {
    try {
      val query = filterRows(dataScope.subspace)

      val result = query
        .groupBy(dataScope.dimension.name)
        .agg(
          dataScope.measure.getAggregationFunction
        )
        .collect()
      result
    } catch {
      case _: NullPointerException =>
        println("No data")
        Array()
    }
  }

  def findDataPatterns(desiredTime: Double): Unit = {
    var depth = 0
    val startTime = System.currentTimeMillis()

    val dimensions: Array[Dimension] = getDimensions
    val temporalDimensions: Array[Dimension] = getTemporalDimensions
    val measures: Array[Measure] = getMeasures

    ExtenderController.setData(this.extenders, dimensions, temporalDimensions, measures)

    while ((desiredTime == .0 || (System.currentTimeMillis() - startTime) < desiredTime * 1000) && depth < dimensions.length) {
      val subspaces = getSubspaces(depth, depth)

      patternTypeResultsMap = Map()

      subspaces
        .flatMap(subspace => {
          val filterDimensions = subspace.array.map(filter => filter.dimension)
          val dividingDimensions = dimensions.filter(dimension => !filterDimensions.contains(dimension))
          dividingDimensions.map(dimension => (subspace, dimension))
        })
        .flatMap(tuple => {
          measures.map(measure => DataScope(tuple._1, tuple._2, measure))
        })
        .foreach(dataScope => {
          println(dataScope)

          val patternTypeResults = getPatternTypeResults(dataScope)
          if (patternTypeResults.length > 0) {
            val homogeneousDataScopes = ExtenderController.getDataScopes(dataScope)
            patternTypeResults.foreach(patternTypeResult => {
              println(patternTypeResult)

              homogeneousDataScopes.foreach(homogeneousDataScope => {
                if (!homogeneousDataScopesSet.contains((homogeneousDataScope, patternTypeResult.patternType))) {
                  homogeneousDataScopesSet += ((homogeneousDataScope, patternTypeResult.patternType))
                  val metaInsight = MetaInsight(patternTypeResult.patternType)
                  homogeneousDataScope.array.foreach(dataScopeOfHomogeneous => {
                    val patternTypeResultsOfHomogeneous = getPatternTypeResults(dataScopeOfHomogeneous)
                    metaInsight.addDataPattern(dataScopeOfHomogeneous, patternTypeResultsOfHomogeneous)
                  })

                  metaInsight.excludeFromSetWithSmallerDensity(this.t)
                  if (!metaInsight.setIsEmpty && !metaInsight.setHasOnlyOneElement) {
                    val conciseness = metaInsight.getConciseness(this.t, this.r, this.k.toDouble, this.g)
                    val impact = getImpact(dataScope) / getImpact(DataScope(superSubspace, dataScope.dimension, dataScope.measure))
                    metaInsight.setScore(impact * conciseness)

                    println(metaInsight)
                    metaInsights += metaInsight
                  }
                }
              })

            })
          }

        })

      depth += 1
    }
  }

  def getInsights: Array[Result] = {
    metaInsights.toArray
      .filter(_.positiveScore)
      .map(_.toResult)
      .sortBy(_.score)
      .reverse
  }

  def getQueryResult(dataScope: DataScope): Array[(Any, Double)] = {
    val queryResult = query(dataScope)
      .map(row => (row(0), row(1).asInstanceOf[Double]))

    queryResult
  }

  def getQueryResultSorted(dataScope: DataScope, patternType: PatternType): Array[(Any, Double)] = {
    val queryResult = getQueryResult(dataScope)
    patternType.sortResults(queryResult)
  }

  private def getTopPatternTypeResults(patternTypeResults: Array[PatternTypeResult]): Array[PatternTypeResult] = {
    ParametersData.advancedSettings.patternTypeFunction.get(patternTypeResults)
  }

  private def getPatternTypeResults(dataScope: DataScope): Array[PatternTypeResult] = {
    val dataScopeString = dataScope.toString
    if (patternTypeResultsMap.contains(dataScopeString)) {
      return patternTypeResultsMap(dataScopeString)
    }

    val queryResult = getQueryResult(dataScope)
    val patternTypeResults = patternTypes
      .map(patternType => patternType.evaluate(dataScope, queryResult))
      .filter(patternTypeResult => patternTypeResult.evaluates)

    val topPatternResults = getTopPatternTypeResults(patternTypeResults)

    patternTypeResultsMap += (dataScopeString -> topPatternResults)
    topPatternResults
  }

}
