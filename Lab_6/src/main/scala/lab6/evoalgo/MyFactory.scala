package lab6.evoalgo

import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory

class MyFactory(dimension: Int, minBound: Double, maxBound: Double) extends AbstractCandidateFactory[MyCandidate]:

  private inline def randomInRange(rng: java.util.Random): Double = 
    rng.nextDouble() * (maxBound - minBound) + minBound

  override def generateRandomCandidate(rng: java.util.Random): MyCandidate = 
    Array.fill(10)(randomInRange(rng))
