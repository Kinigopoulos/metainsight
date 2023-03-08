package com.metainsights.patterns

import com.metainsights.data.{DataScope, Dimension, Measure}

object ExtenderController {

  var extenders: Array[Extender] = _
  var dimensions: Array[Dimension] = _
  var temporalDimensions: Array[Dimension] = _
  var measures: Array[Measure] = _

  def setData(extenders: Array[Extender], dimensions: Array[Dimension], temporalDimensions: Array[Dimension], measures: Array[Measure]): Unit = {
    this.extenders = extenders
    this.dimensions = dimensions
    this.temporalDimensions = temporalDimensions
    this.measures = measures
  }

  def getDataScopes(dataScope: DataScope): Array[HomogeneousDataScope] = {
    extenders
      .flatMap(extender => extender.getDataScopes(dataScope))
      .filter(homogeneousDataScope => homogeneousDataScope.array.length > 1)
  }

}