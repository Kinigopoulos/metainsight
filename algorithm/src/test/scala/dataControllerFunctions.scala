import com.metainsights.data.DataController
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class dataControllerFunctions extends AnyFlatSpec with should.Matchers {

  it should "get all distinct values" in {
    TestHelper.initializeDataController(log = false, "games.csv", "metadata-games-simple.csv")

    val dimensions = DataController.getDimensions

    dimensions.foreach(dimension => {
      val values = DataController.getValuesFromDimension(dimension.name)
      println(dimension.name, values.length, values.toList.distinct.length)
      values.toList.distinct.length should equal (values.length)
    })
  }

}
