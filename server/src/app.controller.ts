import { Controller, Get, Query, Headers, Post, Body } from "@nestjs/common";
import { AppService } from "./app.service";

@Controller("api")
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get("databases")
  async getDatabases(@Query() query, @Headers() headers): Promise<any> {
    return await this.appService.getDatabases(query.broker, {username: headers.username, password: headers.password});
  }

  @Get("database-data")
  async getDatabaseData(@Query() query, @Headers() headers): Promise<any> {
    return await this.appService.getDatabaseData(query.broker, query.database, {username: headers.username, password: headers.password});
  }

  @Post("get-results")
  async getResults(@Body() body): Promise<any> {
    return await this.appService.getResults(body);
  }

}
