import com.metainsights.data.DataController
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object TestHelper {

  def setLogOff(): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
  }

  def initializeDataController(log: Boolean = false, dataName: String = "src/data/test1.csv", metadataName: String = "src/data/metadata1.csv"): Any = {
    if (!log) {
      setLogOff()
    }

    val sparkSession = SparkSession
      .builder()
      .appName("TEST")
      .master("local[*]")
      .getOrCreate()
    val sc = sparkSession.sparkContext

    DataController.readData(sparkSession = sparkSession, sparkContext = sc, dataName = dataName, metadataName = metadataName)
  }

}
