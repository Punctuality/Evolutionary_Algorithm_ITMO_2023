package lab5.evoalgo

import org.uncommons.watchmaker.framework.FitnessEvaluator

import java.util
import cats.syntax.show.*
import scala.collection.immutable.IntMap
import scala.collection.immutable.IntMapUtils

class QueensFitnessFunction extends FitnessEvaluator[QueensSolution]:

  private def countDiagonals(queens: Array[Int]): (IntMap[Int], IntMap[Int]) =
    queens
      .iterator
      .zipWithIndex
      .map:
         (r, c) => (r - c) -> (r + c)
      .foldLeft(IntMap.empty[Int], IntMap.empty[Int]):
        case ((mainDiags, subDiags), (main, sub)) =>
            mainDiags.updated(main, mainDiags.getOrElse(main, 0) + 1) ->
              subDiags.updated(sub, subDiags.getOrElse(sub, 0) + 1)

  override def getFitness(candidate: QueensSolution, population: util.List[_ <: QueensSolution]): Double =
    if candidate.queens.sum != candidate.queens.length * (candidate.queens.length - 1) / 2 then
      throw new IllegalArgumentException(s"Invalid solution: ${candidate.show}")
    else
      
      val (mainDiags, subDiags) = countDiagonals(candidate.queens)
      candidate
        .queens
        .iterator
        .zipWithIndex
        .foldLeft(0):
          case (acc, (r, c)) => 
            val main = r - c
            val sub = r + c
            acc + (mainDiags(main) - 1) + (subDiags(sub) - 1)
        .toDouble
  
  override val isNatural: Boolean = false

