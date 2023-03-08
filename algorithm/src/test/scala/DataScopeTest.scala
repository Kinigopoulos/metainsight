import com.metainsights.data.{DataScope, Dimension, Filter, Measure, Subspace}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class DataScopeTest extends AnyFlatSpec with should.Matchers {

  it should "implement equals() correctly" in {
    var testMap: Map[DataScope, Int] = Map()

    val dataScope1 = DataScope(Subspace(Array(Filter(Dimension("dim1", "temp", 0), 1))), Dimension("dim2", "temp", 1), Measure("meas1", "SUM", 2))
    val dataScope2 = DataScope(Subspace(Array(Filter(Dimension("dim1", "temp", 0), 1))), Dimension("dim2", "temp", 1), Measure("meas1", "SUM", 2))
    val dataScope3 = DataScope(Subspace(Array(Filter(Dimension("dim1", "temp", 0), 1))), Dimension("dim3", "temp", 1), Measure("meas1", "SUM", 2))

    testMap += (dataScope1 -> 1)

    testMap.contains(dataScope2) should equal (true)
    testMap.contains(dataScope3) should equal (false)
  }
}
