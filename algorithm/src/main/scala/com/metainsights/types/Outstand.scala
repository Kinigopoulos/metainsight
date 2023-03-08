package com.metainsights.types

import org.apache.commons.math3.analysis.ParametricUnivariateFunction
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.fitting.AbstractCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoint
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem
import org.apache.commons.math3.linear.DiagonalMatrix
import org.apache.commons.math3.util.FastMath

import java.util

trait Outstand {

  class function extends ParametricUnivariateFunction {
    override def value(x: Double, parameters: Double*): Double = {
      parameters(0) * FastMath.pow(x, parameters(1))
    }

    override def gradient(x: Double, parameters: Double*): Array[Double] = {
      val a = parameters(0)
      val b = parameters(1)

      val aDev = new DerivativeStructure(2, 1, 0, a)
      val bDev = new DerivativeStructure(2, 1, 1, b)
      val y = aDev.multiply(DerivativeStructure.pow(x, bDev))

      Array(y.getPartialDerivative(1, 0), y.getPartialDerivative(0, 1))
    }
  }

  class fitter extends AbstractCurveFitter {
    override def getProblem(points: util.Collection[WeightedObservedPoint]): LeastSquaresProblem = {
      val initialGuess = Array(1.0, -1.0)

      val target = new Array[Double](points.size())
      val weights = new Array[Double](points.size())

      var i = 0
      points.forEach(point => {
        target(i) = point.getY
        weights(i) = point.getWeight
        i += 1
      })

      val model = new AbstractCurveFitter.TheoreticalValuesFunction(new function(), points)

      new LeastSquaresBuilder()
        .maxEvaluations(Integer.MAX_VALUE)
        .maxIterations(Integer.MAX_VALUE)
        .start(initialGuess)
        .target(target)
        .weight(new DiagonalMatrix(weights))
        .model(model.getModelFunction, model.getModelFunctionJacobian)
        .build()
    }
  }
}
