package lab5

import cats.implicits.*
import lab5.eval.CombinedTerminationCondition
import lab5.evoalgo.*
import org.uncommons.watchmaker.framework.*
import org.uncommons.watchmaker.framework.operators.*
import org.uncommons.watchmaker.framework.selection.*
import org.uncommons.watchmaker.framework.termination.*

import scala.jdk.CollectionConverters.*

object Main extends App:
  val dimension = 8
  val populationSize = 100
  val generations = 1000
  val random = java.util.Random()

  val factory = QueensFactory(dimension)
  val operators = List(
    QueensCrossover(5, 0.05),
    QueensMutation(QueensMutation.MutationType.Swap, 0.5)
  )
  val pipeline = EvolutionPipeline(operators.asJava)

  val selection = RouletteWheelSelection()

  val evaluator = QueensFitnessFunction()

  val algorithm = SteadyStateEvolutionEngine(
    factory,
    pipeline,
    evaluator,
    selection,
    populationSize,
    false,
    random
  )

  val observer: EvolutionObserver[QueensSolution] =
    (populationData: PopulationData[_ <: QueensSolution]) =>
      val iter = populationData.getGenerationNumber()
      if iter % 100 == 0 || iter == generations then
          val bestFit = populationData.getBestCandidateFitness();
          System.out.println(s"Generation ${populationData.getGenerationNumber()}: $bestFit");
      if iter % 1000 == 0 || iter == generations then
          val best: QueensSolution = populationData.getBestCandidate();
          System.out.println(s"\tBest solution = ${best.show}")

  algorithm.addEvolutionObserver(observer)

  val terminate = CombinedTerminationCondition(
    CombinedTerminationCondition.CombineType.OR,
    GenerationCount(generations + 1),
    TargetFitness(0.0, evaluator.isNatural)
  )

  val startTime = System.currentTimeMillis()
  val result = algorithm.evolve(populationSize, 1, terminate)
  
  import lab5.evoalgo.QueensSolution.MatrixLike.given
  System.out.println(s"Result (${evaluator.getFitness(result, null)}) = \n${result.show}")
  
  val endTime = System.currentTimeMillis()
  println(s"Time: ${endTime - startTime} ms")
