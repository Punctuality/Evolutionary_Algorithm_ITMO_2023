package lab6.setup

import org.uncommons.watchmaker.framework.FitnessEvaluator

import java.util
import lab6.evoalgo.MyCandidate

class MultiFitnessFunction(dimension: Int, complexity: Int) extends  FitnessEvaluator[MyCandidate]:

  override def getFitness(solution: MyCandidate, list: util.List[_ <: MyCandidate]): Double =
    var result = 0.0

    Range(0, complexity).foreach: _ =>
      result += ackley(solution) / complexity

    return result

  private def ackley(solution: MyCandidate): Double =
    var n = dimension
    var pi = Math.PI
    var dn = 1.0 / n
    var a = 10
    var b = 0.2
    var c = 2 * pi
    var s1 = 0.0
    var s2 = 0.0
    val noise = util.Random(1)

    Range(0, dimension).foreach: i =>
      val value = solution(i) + noise.nextDouble()
      s1 += value * value
      s2 += Math.cos(c * value)

    s1 = -a * Math.exp(-b * Math.sqrt(dn * s1))
    s2 = -Math.exp(dn * s2)
    var result = s1 + s2 + a + Math.exp(1)
    result = -result
    result = result + a
    result = Math.abs(result)
    result

  override val isNatural: Boolean = true