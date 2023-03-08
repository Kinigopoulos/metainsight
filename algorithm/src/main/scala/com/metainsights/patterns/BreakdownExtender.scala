package com.metainsights.patterns

import com.metainsights.data.DataScope

object BreakdownExtender extends Extender {
  override val name: String = "BreakdownExtender"

  override def getDataScopes(dataScope: DataScope): Array[HomogeneousDataScope] = {
    if (!ExtenderController.temporalDimensions.contains(dataScope.dimension)) {
      return Array()
    }
    val dataScopes = ExtenderController.temporalDimensions.map(temporalDimension => {
      dataScope.fromNewDimension(temporalDimension)
    })

    Array(HomogeneousDataScope(dataScopes))
  }
}
