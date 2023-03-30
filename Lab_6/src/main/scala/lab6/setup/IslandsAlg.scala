package lab6.setup

import org.uncommons.watchmaker.framework.*
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection
import org.uncommons.watchmaker.framework.termination.GenerationCount

import java.util

import lab6.evoalgo.*
import org.uncommons.watchmaker.framework.islands.IslandEvolution
import org.uncommons.watchmaker.framework.islands.RingMigration
import org.uncommons.watchmaker.framework.islands.IslandEvolutionObserver

object IslandsAlg extends App:

  val dimension = 10 // dimension of problem
  val complexity = 5 // fitness estimation time multiplicator
  val populationSize = 50 // size of population
  val epochLength = 50
  val islandCount = Runtime.getRuntime().availableProcessors() / 2
  val generations = 1000 / epochLength // number of generations

  val random = new util.Random() // random

  val factory = new MyFactory(dimension, -5, 5) // generation of solutions

  val operators = new util.ArrayList[EvolutionaryOperator[MyCandidate]]()
  operators.add(new MyCrossover()) // Crossover
  operators.add(new MyMutation(-5, 5, 1, 1)) // Mutation

  val pipeline = new EvolutionPipeline[MyCandidate](operators)

  val selection = new RouletteWheelSelection() // Selection operator

  val evaluator = new MultiFitnessFunction(dimension, complexity) // Fitness function

  val islandMigration = new RingMigration
  val islandsAlgorithm = new IslandEvolution[MyCandidate](
    islandCount, 
    islandMigration,
    factory,
    pipeline,
    evaluator,
    selection,
    random
  )
  
  islandsAlgorithm.addEvolutionObserver:
    new IslandEvolutionObserver[MyCandidate]:
      override def populationUpdate(data: PopulationData[_ <: MyCandidate]): Unit =
        val bestFit = data.getBestCandidateFitness();
        println("Epoch " + data.getGenerationNumber() + ": " + bestFit);
        println("\tEpoch best solution = " + util.Arrays.toString(data.getBestCandidate()));

      override def islandPopulationUpdate(islandIndex: Int, data: PopulationData[_ <: MyCandidate]): Unit = 
        val bestFit = data.getBestCandidateFitness()
        println("Island " + islandIndex)
        println("\tGeneration " + data.getGenerationNumber() + ": " + bestFit)
        println("\tBest solution = " + util.Arrays.toString(data.getBestCandidate()))


  val terminate = new GenerationCount(generations)

  val startTime = System.currentTimeMillis()
  islandsAlgorithm.evolve(populationSize, 1, epochLength, 2, terminate)
  println(s"Time: ${(System.currentTimeMillis() - startTime) / 1000.0} s")
