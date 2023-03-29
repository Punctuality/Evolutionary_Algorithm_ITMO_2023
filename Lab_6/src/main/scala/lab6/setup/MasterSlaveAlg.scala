package lab6.setup

import org.uncommons.watchmaker.framework.*
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection
import org.uncommons.watchmaker.framework.termination.GenerationCount

import java.util

import lab6.evoalgo.*

object MasterSlaveAlg extends App:

  val dimension = 10 // dimension of problem
  val complexity = 5 // fitness estimation time multiplicator
  val populationSize = 50 // size of population
  val generations = 1000 // number of generations

  val random = new util.Random() // random

  val factory = new MyFactory(dimension, -5, 5) // generation of solutions

  val operators = new util.ArrayList[EvolutionaryOperator[MyCandidate]]()
  operators.add(new MyCrossover()) // Crossover
  operators.add(new MyMutation(-5, 5, 1, 1)) // Mutation

  val pipeline = new EvolutionPipeline[MyCandidate](operators)

  val selection = new RouletteWheelSelection() // Selection operator

  val evaluator = new MultiFitnessFunction(dimension, complexity) // Fitness function

  val algorithm = new SteadyStateEvolutionEngine[MyCandidate](
          factory, pipeline, evaluator, selection, populationSize, false, random)

  algorithm.setSingleThreaded(false)

  // algorithm.addEvolutionObserver: (data: PopulationData[_ <: MyCandidate]) => 
  //   val bestFit = data.getBestCandidateFitness()
  //   println("Generation " + data.getGenerationNumber() + ": " + bestFit)
  //   println("\tBest solution = " + util.Arrays.toString(data.getBestCandidate()))

  val terminate = new GenerationCount(generations)

  val startTime = System.currentTimeMillis()
  algorithm.evolve(populationSize, 1, terminate)
  println(s"Time: ${(System.currentTimeMillis() - startTime) / 1000.0} s")
