package lab5.evoalgo

import lab5.util.ArrayUtil.*

import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory

import java.util.Random

class QueensFactory(dimension: Int) extends AbstractCandidateFactory[QueensSolution]:
  override def generateRandomCandidate(rng: Random): QueensSolution = 
    val queens = Array.range(0, dimension)
    queens.scramble(rng)
    QueensSolution(queens)