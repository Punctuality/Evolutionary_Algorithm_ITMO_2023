package lab6.evoalgo

import lab6.util.ArrayUtil.*

import org.uncommons.watchmaker.framework.operators.AbstractCrossover
import java.{util => ju}
import com.google.common.collect.ImmutableList

class MyCrossover extends AbstractCrossover[MyCandidate](1):

  override def mate(p1: MyCandidate, p2: MyCandidate, numberOfCrossoverPoints: Int, rng: ju.Random): ju.List[MyCandidate] = 
    if p1.length != p2.length then
      throw IllegalArgumentException("Input candidates cannot have different dimensions on crossover")

    val result = ImmutableList.of(p1.clone, p2.clone)

    Range(0, numberOfCrossoverPoints).foreach: _ =>
      val idx = rng.nextInt(p1.length)
      result.get(0).swap(idx, result.get(1))

    result
