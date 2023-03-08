import { Injectable } from "@nestjs/common";
import axios from "axios";
import * as fs from "fs";
import * as path from "path";
import { Parser } from "json2csv";
import { spawn, spawnSync } from "child_process";
import * as crypto from "crypto";
import { readFileSync } from "fs";

// Relative file path to save the data
const sparkSubFolder = "spark";
const filePath = (fileName) => path.join(".", sparkSubFolder, fileName);
const sparkProgramPath = `./${sparkSubFolder}/metainsights-1.0.jar`;
const resultsPartFileName = "part-00000";

function generateHashCode(input: string) {
  const hash = crypto.createHash("sha256");
  hash.update(input);
  return hash.digest("hex");
}

@Injectable()
export class AppService {

  __timeColumns = [
    "__time_Year",
    "__time_Month",
    "__time_Day",
    "__time_DayOfWeek",
    "__time_Hour",
    "__time_Minute",
    "__time_Second"
  ];

  __timeColumnsFunctions = {
    "__time_Year": (timestamp) => {
      return new Date(timestamp).getFullYear();
    },
    "__time_Month": (timestamp) => {
      return new Date(timestamp).getMonth();
    },
    "__time_Day": (timestamp) => {
      return new Date(timestamp).getDate();
    },
    "__time_DayOfWeek": (timestamp) => {
      return new Date(timestamp).getDay();
    },
    "__time_Hour": (timestamp) => {
      return new Date(timestamp).getHours();
    },
    "__time_Minute": (timestamp) => {
      return new Date(timestamp).getMinutes();
    },
    "__time_Second": (timestamp) => {
      return new Date(timestamp).getSeconds();
    }
  };

  async getDatabases(broker: string, { username, password }): Promise<any> {
    const response = await axios.get(`${broker}/druid/v2/datasources`, { auth: { username, password } });
    return response.data;
  }

  async getDatabaseData(broker: string, database: string, { username, password }): Promise<any> {
    const response = await axios.post(`${broker}/druid/v2/`, {
        "queryType": "segmentMetadata",
        "dataSource": database
      },
      { auth: { username, password } });

    const columns = new Set();
    for (const segment of response.data) {
      const segmentColumns = Object.keys(segment.columns);
      segmentColumns.forEach(column => columns.add(column));
    }
    const includesTimeColumn = columns.has("__time");
    if (includesTimeColumn) {
      columns.delete("__time");
    }

    return includesTimeColumn ? [...this.__timeColumns, ...columns] : [...columns];
  }

  readResults(resultsPath) {
    try {
      const resultsString = fs.readFileSync(`${resultsPath}/${resultsPartFileName}`, "utf-8");
      const results = JSON.parse(resultsString);

      return {
        success: true,
        results: results
      };
    } catch (e) {
      console.log(e);
      return { success: false };
    }
  }

  async getResults(body: any): Promise<any> {
    const { query, broker, router, username, password, debugMode = true } = body;
    const { database, dimensions, desiredTime, advancedSettings } = query;

    const hashObject = JSON.stringify({database, dimensions, desiredTime, advancedSettings});
    const hashCode = generateHashCode(hashObject);

    const timestamp = Date.now();
    const dataFileName = `data-${database}-${timestamp}.csv`;
    const metadataFileName = `metadata-${database}-${timestamp}.csv`;
    const resultsFileName = `results-${hashCode}`;
    const resultsPath = filePath(resultsFileName);
    const parametersString = JSON.stringify({
      resultsPath: resultsPath,
      desiredTime: desiredTime || .0,
      advancedSettings
    });

    try {
      const fileExistsCheck = fs.statSync(resultsPath);
      if (fileExistsCheck.isDirectory()) {
        console.log("Results have already been generated. Skipping spark-submit...");
        return this.readResults(resultsPath);
      }
    } catch (e) {

    }

    let selectDimensions = dimensions.map(dimension => dimension.dimensionName);
    let timeColumns = [];
    const includesTimeColumn = selectDimensions.some(dimension => this.__timeColumns.includes(dimension));
    if (includesTimeColumn) {
      timeColumns = selectDimensions.filter(dimension => this.__timeColumns.includes(dimension));
      selectDimensions = selectDimensions.filter(dimension => !this.__timeColumns.includes(dimension));
      selectDimensions.push("__time");
    }

    const druidQuery = {
      queryType: "scan",
      dataSource: database,
      columns: selectDimensions,
      granularity: "all",
      intervals: "0/10000"
    };

    const axiosResponse = await axios.post(`${broker}/druid/v2`, druidQuery, { auth: { username, password } });

    const druidData = axiosResponse.data.flatMap(obj => {
      return obj.events.map(event => {
        if (!event.__time) {
          return event;
        }
        const time = event.__time;
        delete event.__time;
        for (const timeColumn of timeColumns) {
          event[timeColumn] = this.__timeColumnsFunctions[timeColumn](time);
        }
        return event;
      });
    });

    const parser = new Parser();
    const csv = parser.parse(druidData);

    const dataPathName = filePath(dataFileName);
    await fs.writeFileSync(dataPathName, csv);

    const metadata = dimensions
      .flatMap(dimension => {
        if (dimension.aggregationFunctions.length > 0) {
          return dimension.aggregationFunctions.map(aggregationFunction => ({...dimension, aggregationFunction}));
        }
        return dimension;
      })
      .map(dimension => {
        return {
          Name: dimension.dimensionName,
          CategoryType: dimension.categoryType,
          Aggregation: dimension.aggregationFunction || ""
        };
      });
    const metadataCsv = parser.parse(metadata);

    const metadataPathName = filePath(metadataFileName);
    await fs.writeFileSync(metadataPathName, metadataCsv);

    const sparkMasterUrl = "spark://localhost:7077";


    function runSpark() {
      return new Promise((resolve) => {
        const args = ["--master", sparkMasterUrl, sparkProgramPath, dataPathName, metadataPathName, parametersString];
        if (!debugMode) {
          const sparkSubmitExecution = spawnSync("spark-submit", args);
          const code = sparkSubmitExecution.status;
          if (code !== 0) {
            console.log(sparkSubmitExecution.stderr.toString());
          }
          console.log(`Child process exited with code ${code}`);
          resolve(code === 0);
          return;
        }
        const child = spawn("spark-submit", args);

        child.stdout.on("data", (data) => {
          console.log(`${data}`);
        });

        child.stderr.on("data", (data) => {
          console.error(`ERROR: ${data}`);
        });

        child.on("close", (code) => {
          console.log(`child process exited with code ${code}`);
          resolve(code === 0);
        });
      });
    }
    const success = await runSpark();

    if (!success) {
      return {
        success: false
      };
    }

    return this.readResults(resultsPath);
  }

}
