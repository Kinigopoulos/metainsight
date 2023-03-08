package com.metainsights.patterns

import com.metainsights.data.{DataController, DataScope, Subspace}

object SubspaceExtender extends Extender {
  override val name: String = "SubspaceExtender"

  override def getDataScopes(dataScope: DataScope): Array[HomogeneousDataScope] = {
    val subspace = dataScope.subspace
    if (subspace.array.length == 0) {
      return Array()
    }

    subspace.array.map(filter => {
      val values = DataController.getValuesFromDimension(filter.dimension.name)
      val index = subspace.array.indexOf(filter)

      val dimensionSubspace = values.map(value => {
        val filters = subspace.array.clone()
        filters(index) = filter.createFilter(value)
        dataScope.fromNewSubspace(Subspace(filters))
      })

      HomogeneousDataScope(dimensionSubspace)
    })
  }

}
