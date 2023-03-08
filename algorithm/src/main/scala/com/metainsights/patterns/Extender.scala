package com.metainsights.patterns

import com.metainsights.data.DataScope

trait Extender {

  val name: String

  def getDataScopes(dataScope: DataScope): Array[HomogeneousDataScope]

}
