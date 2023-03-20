package lab5.evoalgo

import org.uncommons.watchmaker.framework.FitnessEvaluator

import java.util

class QueensFitnessFunction extends FitnessEvaluator[QueensSolution]:
  override def getFitness(candidate: QueensSolution, population: util.List[_ <: QueensSolution]): Double =
    if candidate.queens.sum != candidate.queens.length * (candidate.queens.length - 1) / 2 then
      throw new IllegalArgumentException("Invalid solution")
    else
      candidate
        .queens
        .indices
        .foldLeft(0):
          case (acc, idx) => acc + candidate.intersectionsOn(idx)
        .toDouble
  
  override val isNatural: Boolean = false

