import com.metainsights.data.DataController
import org.scalatest.flatspec._
import org.scalatest.matchers._

class getSubspacesTest extends AnyFlatSpec with should.Matchers {

  it should "get Subspaces" in {
    TestHelper.initializeDataController()
    DataController.getSubspaces(0, 5)
  }

}
