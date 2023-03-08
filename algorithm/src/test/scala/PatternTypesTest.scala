import com.metainsights.ParametersData
import com.metainsights.data._
import com.metainsights.types._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PatternTypesTest extends AnyFlatSpec with should.Matchers {

  private val patternTypes: Array[PatternType] = ParametersData.allPatternTypes

  val nonTemporalDataScope: DataScope = DataScope(Subspace(Array(Filter(Dimension("Dimension01", "Categorical", 1), 10))), Dimension("Dimension02", "Categorical", 2), Measure("Measure01", "SUM", 5))
  val temporalDataScope: DataScope = DataScope(Subspace(Array(Filter(Dimension("Dimension01", "Categorical", 1), 10))), Dimension("Dimension02", "Temporal", 2), Measure("Measure01", "SUM", 5))

  val testResults: Map[String, Array[(Any, Double)]] = {
    Map(
      "test01" -> Array(("row1", 1), ("row2", 2), ("row3", 3), ("row4", 4), ("row5", 5), ("row6", 6), ("row7", 7)),
      "test02" -> Array(("row1", 1), ("row2", 2), ("row3", 3), ("row4", 4), ("row5", 5), ("row6", 6), ("row7", 8)),
      "test03" -> Array(("row1", 1), ("row2", 2), ("row3", 3), ("row4", -10), ("row5", 5), ("row6", 6), ("row7", 7)),
      "test04" -> Array(("row1", 10), ("row2", 2), ("row3", 11), ("row4", 12), ("row5", 13), ("row6", 14), ("row7", 15)),
      "test05" -> Array(("row1", 10), ("row2", 8), ("row3", 11), ("row4", 12), ("row5", 13), ("row6", 14), ("row7", 15)),
      "test06" -> Array(("row1", 3), ("row2", 3), ("row3", 3), ("row4", 3), ("row5", 3), ("row6", 3), ("row7", 3)),
      "test07" -> Array(("row1", 3), ("row2", 3), ("row3", 3), ("row4", 3), ("row5", 7), ("row6", 8), ("row7", 29)),
      "test08" -> Array(("row1", 3), ("row2", 3), ("row3", 3), ("row4", 3), ("row5", 89), ("row6", 3), ("row7", 3)),
      "test09" -> Array((1.0, 3.0), (2.0, 3.0), (3.0, 3.0), (4.0, 3.0), (5.0, 89.0), (6.0, 3.0), (7.0, 3.0)),
      "test10" -> Array((1.0, 3.0), (2.0, 3.0), (3.0, 3.0), (4.0, 3.0), (5.0, 3.0), (6.0, 3.0), (7.0, 113.0)),
      "test11" -> Array((1.0, 3.0), (2.0, 3.0), (3.0, 3.0), (4.0, 3.0), (5.0, 3.0), (6.0, 3.0), (7.0, 4.0)),
      "test12" -> Array((1.0, 7.0), (2.0, 5.0), (3.0, 2.0), (4.0, 4.0), (5.0, 6.0), (6.0, 4.0), (7.0, 5.0)),
      "test13" -> Array((1.0, 7.0), (2.0, 5.0), (3.0, 7.0), (4.0, 5.0), (5.0, 9.0), (6.0, 4.0), (7.0, 5.0)),
      "test14" -> Array((1.0, 9.0), (2.0, 23.0), (3.0, 37.0), (4.0, 12.0), (5.0, 12.0)),
    )
  }

  def greaterProbabilityThan(patternType: PatternType, greaterSet: Array[(Any, Double)], smallerSet: Array[(Any, Double)]): Boolean = {
    val dataScope = if (greaterSet(0)._1.isInstanceOf[Double] || smallerSet(0)._1.isInstanceOf[Double]) temporalDataScope else nonTemporalDataScope
    val patternTypeResultGreater = patternType.evaluate(dataScope, greaterSet)
    val patternTypeResultSmaller = patternType.evaluate(dataScope, smallerSet)
    println(greaterSet.mkString("Array(", ", ", ")"))
    println(smallerSet.mkString("Array(", ", ", ")"))
    println(s"Greater: ${patternTypeResultGreater}")
    println(s"Smaller: ${patternTypeResultSmaller}")
    patternTypeResultGreater.score >= patternTypeResultSmaller.score
  }

  it should "work with no errors" in {
    val resultsArray: Array[(String, Array[(Any, Double)])] = testResults.toArray.map(test => (test._1, test._2))
    patternTypes.foreach(patternType => {
      println(s"=== Results for ${patternType.name} ===")
      resultsArray.sortBy(_._1).foreach(resultsTuple => {
        val testName = resultsTuple._1
        val results = resultsTuple._2
        val dataScope = if (results(0)._1.isInstanceOf[Double]) temporalDataScope else nonTemporalDataScope
        val patternTypeResult = patternType.evaluate(dataScope, results)
        println(s"${testName} - ${patternTypeResult}")
      })
      println("============================")
    })
  }


  it should "yield bigger probability for small results" in {
    greaterProbabilityThan(OutstandingLast, testResults("test04"), testResults("test05")) should be (true)
    greaterProbabilityThan(OutstandingLast, testResults("test04"), testResults("test02")) should be (true)
    greaterProbabilityThan(OutstandingLast, testResults("test01"), testResults("test06")) should be (true)
  }

  it should "yield results for evenness" in {
    greaterProbabilityThan(Evenness, testResults("test06"), testResults("test14")) should be (true)
    greaterProbabilityThan(Evenness, testResults("test06"), testResults("test11")) should be (true)
  }

}