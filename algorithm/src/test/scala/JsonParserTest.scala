import com.metainsights.ParametersData
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class JsonParserTest extends AnyFlatSpec with should.Matchers {

  it should "parse json string" in {
    val jsonString = "{resultsPath:'results4',desiredTime:0,advancedSettings:{balancingParameters:{t:0.3,r:1,k:3,g:0.1},extenders:['SubspaceExtender','DimensionExtender','MeasureExtender'],patternTypes:['OutstandingFirst','OutstandingLast','Evenness','Trend']}}"
    ParametersData.fromJson(jsonString)
    println(ParametersData.resultsPath)

  }

}
