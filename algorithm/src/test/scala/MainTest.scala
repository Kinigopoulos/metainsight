import com.metainsights.Main
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class MainTest extends AnyFlatSpec with should.Matchers {

  it should "run example dataset" in {
    TestHelper.setLogOff()

    Main.dataName = "src/data/test1.csv"
    Main.metadataName = "src/data/metadata1.csv"

    val results = Main.getResults
    val json = Main.toJSON(results)
    println(json)
  }

  it should "run products example" in {
    TestHelper.setLogOff()

    Main.dataName = "src/data/products.csv"
    Main.metadataName = "src/data/metadata-products.csv"

    val results = Main.getResults
    val json = Main.toJSON(results)
    println(json)
  }

}
