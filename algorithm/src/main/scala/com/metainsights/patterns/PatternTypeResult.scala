package com.metainsights.patterns

import com.metainsights.types.PatternType

case class PatternTypeResult(patternType: PatternType, evaluates: Boolean, score: Double, highlight: Any) {

  override def toString: String = s"PatternTypeResult(${patternType}, ${evaluates}, ${score}, ${highlight})"

  override def canEqual(that: Any): Boolean = that.isInstanceOf[PatternTypeResult] && this.highlight.isInstanceOf[Array[_]] == that.asInstanceOf[PatternTypeResult].highlight.isInstanceOf[Array[_]]

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: PatternTypeResult =>
        var equalHighlight = false
        if (this.highlight.isInstanceOf[Array[_]] && that.highlight.isInstanceOf[Array[_]]) {
          equalHighlight = this.highlight.asInstanceOf[Array[Any]].mkString(",") == that.highlight.asInstanceOf[Array[Any]].mkString(",")
        } else {
          equalHighlight = this.highlight.toString == that.highlight.toString
        }
        equalHighlight && this.patternType == that.patternType
      case _ => false
    }
  }
}
