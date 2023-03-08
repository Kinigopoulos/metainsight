package com.metainsights

import com.google.gson.{JsonArray, JsonElement, JsonNull, JsonObject, JsonPrimitive}
import com.metainsights.patterns.Result

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

object JsonHelper {
  private def JSON(props: (String, JsonElement)*): JsonObject = {
    props.foldLeft(new JsonObject)((json, pair) => {
      json.add(pair._1, pair._2)
      json
    })
  }

  private def JsonValue(value: Int): JsonPrimitive = new JsonPrimitive(value)

  private def JsonValue(value: Double): JsonPrimitive = new JsonPrimitive(value)

  private def JsonValue(value: String): JsonPrimitive = new JsonPrimitive(value)

  private def JsonValue(value: Long): JsonPrimitive = new JsonPrimitive(value)

  private def JsonValue(value: Boolean): JsonPrimitive = new JsonPrimitive(value)

  private def JsonValue(value: Character): JsonPrimitive = new JsonPrimitive(value)

  private def JsonValue(): JsonNull = JsonNull.INSTANCE

  private def JsonValue(value: Any): JsonPrimitive = {
//    if (value.isInstanceOf[Array[_]]) {
//      val list =  value.asInstanceOf[Array[Any]].toList.map(v => JsonValue(v))
//      val jsonArray = listToArray(list)
//      return JsonValue(convertToJson(jsonArray).toString)
//    }
    val str = value.toString
    val resultInt = Try(str.toInt)
    resultInt match {
      case Success(value) => return JsonValue(value)
      case Failure(_) =>
    }
    val resultLong = Try(str.toLong)
    resultLong match {
      case Success(value) => return JsonValue(value)
      case Failure(_) =>
    }
    val resultDouble = Try(str.toDouble)
    resultDouble match {
      case Success(value) => return JsonValue(value)
      case Failure(_) =>
    }
    JsonValue(str)
  }

  implicit def listToArray(lis: List[Any]): JsonArray =
    lis.foldLeft(new JsonArray)((array, item) => {
      array.add(convertToJson(item))
      array
    })

  private def convertToJson(obj: Any): JsonElement = obj match {
    case e: JsonElement => e
    case lis: List[Any] => listToArray(lis)
    case s: String => new JsonPrimitive(s)
    case n: Number => new JsonPrimitive(n)
    case b: Boolean => new JsonPrimitive(b)
    case null => JsonNull.INSTANCE
    case _ => throw new RuntimeException
  }

  def getJSON(results: Array[Result]): String = {
    println("Producing json response...")
    val json = JSON(
      "insights" -> results.toList.map(result => {
        JSON(
          "scopes" -> result.scopes.toList.map(scope => {
            val category = scope._1
            val dataScope = scope._2
            val resultSet = scope._3
            val patternTypeResult = scope._4
            val patternTypeResultExists = patternTypeResult != null

            var categoryString = category
            var highlightArray: JsonArray = null
            if (patternTypeResultExists && patternTypeResult.highlight != null) {
              if (patternTypeResult.highlight.isInstanceOf[Array[_]]) {
                val list = patternTypeResult.highlight.asInstanceOf[Array[Any]].toList.map(v => JsonValue(v))
                highlightArray = listToArray(list)
              }

              categoryString = category + " / " + (if (highlightArray != null) convertToJson(highlightArray).toString else patternTypeResult.highlight)
            }

            JSON(
              "category" -> JsonValue(categoryString),
              "dataScope" -> JSON(
                "subspace" -> dataScope.subspace.array.toList.map(filter => JSON("dimension" -> JsonValue(filter.dimension.name), "value" -> JsonValue(filter.value))),
                "breakdownDimension" -> JsonValue(dataScope.dimension.name),
                "measure" -> JsonValue(dataScope.measure.name),
                "aggregationFunction" -> JsonValue(dataScope.measure.aggregationType)
              ),
              "resultSet" -> resultSet.toList.map(tuple => JSON("label" -> JsonValue(tuple._1), "value" -> JsonValue(tuple._2))),
              "patternTypeResult" -> JSON(
                "patternType" -> (if (patternTypeResultExists) JsonValue(patternTypeResult.patternType.name) else JsonValue()),
                "evaluates" -> (if (patternTypeResultExists) JsonValue(patternTypeResult.evaluates) else JsonValue()),
                "score" -> (if (patternTypeResultExists) JsonValue(patternTypeResult.score) else JsonValue()),
                "highlight" -> (if (highlightArray != null) highlightArray else if (patternTypeResultExists && patternTypeResult.highlight != null) JsonValue(patternTypeResult.highlight) else JsonValue())
              )
            )
          }),
          "score" -> JsonValue(result.score),
          "setsSize" -> JsonValue(result.setsSize),
          "exceptionsSize" -> JsonValue(result.exceptionsSize)
        )
      })
    )

    convertToJson(json).toString
  }
}