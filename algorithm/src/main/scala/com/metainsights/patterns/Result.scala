package com.metainsights.patterns

import com.metainsights.data.DataScope

case class Result(
                 scopes: Array[(String, DataScope, Array[(Any, Double)], PatternTypeResult)],
                 score: Double,
                 setsSize: Int,
                 exceptionsSize: Int
                 ) {

}
