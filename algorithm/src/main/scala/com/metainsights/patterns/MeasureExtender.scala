package com.metainsights.patterns

import com.metainsights.data.DataScope

object MeasureExtender extends Extender {
  override val name: String = "MeasureExtender"

  override def getDataScopes(dataScope: DataScope): Array[HomogeneousDataScope] = {
    val dataScopes = ExtenderController.measures.map(measure => {
      dataScope.fromNewMeasure(measure)
    })

    Array(HomogeneousDataScope(dataScopes))
  }
}
