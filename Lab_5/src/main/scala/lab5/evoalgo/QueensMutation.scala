package lab5.evoalgo

import lab5.util.ArrayUtil.*
import org.uncommons.watchmaker.framework.EvolutionaryOperator

import java.util
import java.util.Random
import scala.jdk.CollectionConverters.*
import scala.math.*

class QueensMutation(
  mutationType: QueensMutation.MutationType,
  mutationProbability: Double
) extends EvolutionaryOperator[QueensSolution]:
  import QueensMutation.MutationType.*

  private def mutate(candidate: QueensSolution, rng: Random): QueensSolution = 
    val newSolution = candidate.queens.clone()
    mutationType match
      case Swap => 
        val idx1 = rng.nextInt(candidate.queens.length)
        val idx2 = rng.nextInt(candidate.queens.length)
        newSolution.swap(idx1, idx2)

      case LocalSwap(maxRadius) => 
        val idx1 = rng.nextInt(candidate.queens.length)
        val radius = rng.nextInt(maxRadius) + 1
        val idx2 = (idx1 + radius) % candidate.queens.length
        newSolution.swap(idx1, idx2)

      case Insertion => 
        val idx1 = rng.nextInt(candidate.queens.length - 1)
        var idx2 = rng.nextInt(candidate.queens.length)
        while (idx1 == idx2)
          idx2 = rng.nextInt(candidate.queens.length)

        val (left, right) = (min(idx1, idx2), max(idx1, idx2))

        newSolution(left) = candidate.queens(right)
        System.arraycopy(
          candidate.queens,
          left + 1,
          newSolution,
          left + 2,
          right - left - 1
        )

      case Scramble(maxLen) => 
        val scrambleSegmentLen = rng.nextInt(maxLen) + 1
        val scrambleStart = rng.nextInt(candidate.queens.length - scrambleSegmentLen)
        val scrambleSection = newSolution.slice(scrambleStart, scrambleStart + scrambleSegmentLen)
        scrambleSection.scramble(rng)

        System.arraycopy(scrambleSection, 0, newSolution, scrambleStart, scrambleSegmentLen)

      case Inversion(maxLen) => 
        val inversionSegmentLen = rng.nextInt(maxLen) + 1
        val inversionStart = rng.nextInt(candidate.queens.length - inversionSegmentLen)
        val inversionSection = newSolution.slice(inversionStart, inversionStart + inversionSegmentLen).reverse

        System.arraycopy(inversionSection, 0, newSolution, inversionStart, inversionSegmentLen)
    end match

    QueensSolution(newSolution)
    

  override def apply(selectedCandidates: util.List[QueensSolution], rng: Random): util.List[QueensSolution] = 
    val result = new util.ArrayList[QueensSolution](selectedCandidates.size)
    Range(0, selectedCandidates.size())
      .foreach: idx => 
        selectedCandidates.get(idx) match
          case candidate if rng.nextDouble() < mutationProbability => 
            result add mutate(candidate, rng)
          case same => result add same
    
    result

object QueensMutation:
  enum MutationType:
    case Swap
    case LocalSwap(maxRadius: Int)
    case Insertion
    case Scramble(maxLen: Int)
    case Inversion(maxLen: Int)
