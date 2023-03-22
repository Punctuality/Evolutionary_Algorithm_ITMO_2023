package lab5.evoalgo

import org.uncommons.watchmaker.framework.operators.AbstractCrossover
import org.uncommons.maths.random.Probability
import java.util
import com.google.common.collect.ImmutableList
import java.util.Arrays

class QueensCrossover(maxLenSwappedProc: Int, crossoverProbability: Double) 
  extends AbstractCrossover[QueensSolution](1, Probability(crossoverProbability)):

  private def orderCrossover(from: Array[Int], to: Array[Int], rng: util.Random): Array[Int] =
    val toNew = to.clone

    val maxLenSwapped = (maxLenSwappedProc * from.length / 100.0).ceil.toInt
    val swapSegmentLen = rng.nextInt(maxLenSwapped) + 1
    val swapPoint = rng.nextInt(from.length - swapSegmentLen)
    val swapSegment = from.slice(swapPoint, swapPoint + swapSegmentLen)

    System.arraycopy(swapSegment, 0, toNew, swapPoint, swapSegmentLen)
    Arrays.sort(swapSegment)

    var j = 0
    var jumped = false
    for i <- 0 until to.length do
      if j >= swapPoint && !jumped then
          j += swapSegmentLen
          jumped = true
      if Arrays.binarySearch(swapSegment, to(i)) < 0 then
        toNew(j) = to(i)
        j += 1
    end for

    toNew

  override def mate(
    parent1: QueensSolution,
    parent2: QueensSolution,
    numberOfCrossoverPoints: Int,
    rng: util.Random
  ): util.List[QueensSolution] = 
    if parent1.queens.length != parent2.queens.length then
      throw new IllegalStateException("Input candidates cannot have different dimensions on crossover")

    var child1 = parent1.queens
    var child2 = parent2.queens
    Range(0, numberOfCrossoverPoints).foreach: _ =>
      val tmp = orderCrossover(child1, child2, rng)
      child1 = orderCrossover(child2, child1, rng)
      child2 = tmp

    ImmutableList.of(QueensSolution(child1), QueensSolution(child2))