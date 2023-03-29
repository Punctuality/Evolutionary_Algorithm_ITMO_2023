package lab6.eval.perf

import cats.*
import cats.effect.*
import cats.syntax.all.*
import cats.effect.syntax.all.*

import lab6.eval.ExperimentRunner
import lab6.evoalgo.*
import lab6.setup.MultiFitnessFunction

import org.uncommons.watchmaker.framework.*
import org.uncommons.watchmaker.framework.operators.*
import org.uncommons.watchmaker.framework.selection.*

import com.google.common.util.concurrent.AtomicDouble
import java.util.concurrent.atomic.AtomicInteger
import java.util

import scala.jdk.CollectionConverters.*
import org.uncommons.watchmaker.framework.islands.RingMigration
import org.uncommons.watchmaker.framework.islands.IslandEvolution
import org.uncommons.watchmaker.framework.islands.IslandEvolutionObserver
import org.uncommons.watchmaker.framework.termination.GenerationCount
import scala.annotation.migration

class PerfExperimentRunner[F[_]: Async: Clock](repeats: Int)
  extends ExperimentRunner[F, PerfExperimentSetup, List[PerfExperimentResult]]:

  private def runExperiment(
      algorithm: AbstractEvolutionEngine[MyCandidate] | IslandEvolution[MyCandidate],
      setup: PerfExperimentSetup
    ): F[PerfExperimentResult] = 

      val bestFit = AtomicDouble(Double.MaxValue)
      val firstAchievedIter = AtomicInteger(0)
      val lastGeneration = AtomicInteger(0)


      algorithm match
        case island: IslandEvolution[MyCandidate] =>
          island.addEvolutionObserver:
            new IslandEvolutionObserver[MyCandidate]:
              override def populationUpdate(data: PopulationData[_ <: MyCandidate]): Unit =
                val currentFit = data.getBestCandidateFitness()
                lastGeneration.set(data.getGenerationNumber())
                if currentFit < bestFit.get() then
                  bestFit.set(currentFit)
                  firstAchievedIter.set(data.getGenerationNumber())

              override def islandPopulationUpdate(islandIndex: Int, data: PopulationData[_ <: MyCandidate]): Unit = ()

        case ordinary: AbstractEvolutionEngine[MyCandidate] =>
          ordinary.addEvolutionObserver:
            (populationData: PopulationData[_ <: MyCandidate]) =>
              val currentFit = populationData.getBestCandidateFitness()
              lastGeneration.set(populationData.getGenerationNumber())
              if currentFit < bestFit.get() then
                bestFit.set(currentFit)
                firstAchievedIter.set(populationData.getGenerationNumber())

      Clock[F]
        .timed:
          Async[F].delay:
            (algorithm -> setup.algoType) match
              case (
                islands: IslandEvolution[MyCandidate],
                PerfExperimentSetup.AlgoType.Islands(_, epochLen, migrationCount)
                ) =>
                val terminate = GenerationCount((setup.generations + 1) / epochLen)
                islands.evolve(setup.populationSize, 1, epochLen, migrationCount,  terminate)
              case (ordinary: AbstractEvolutionEngine[MyCandidate], _) =>
                ordinary.evolve(setup.populationSize, 1, GenerationCount(setup.generations + 1))
              case _ => throw new IllegalArgumentException("Unknown algorithm and setup combination")
        .map:
          case (duration, solution) => 
            PerfExperimentResult(
              bestFit.get(),
              solution,
              firstAchievedIter.get(),
              lastGeneration.get(),
              duration.toMillis
            )


  override def run(setup: PerfExperimentSetup): F[List[PerfExperimentResult]] = 
    Async[F]
      .delay:
        val factory = MyFactory(setup.dimension, -5, 5)

        val evaluator = new MultiFitnessFunction(setup.dimension, setup.complexity) // Fitness function

        val operators: List[EvolutionaryOperator[MyCandidate]] = 
          MyCrossover() :: MyMutation(-5, 5, 2, 0.1) :: Nil

        val pipeline = EvolutionPipeline(operators.asJava)

        val selection = new RouletteWheelSelection()

        def algo(iter: Int): AbstractEvolutionEngine[MyCandidate] | IslandEvolution[MyCandidate] = setup.algoType match
          case PerfExperimentSetup.AlgoType.Islands(islandCount, _, _) =>
            val islandMigration = RingMigration()
            new IslandEvolution[MyCandidate](
              islandCount, 
              islandMigration,
              factory,
              pipeline,
              evaluator,
              selection,
              util.Random(iter)
            )
          case PerfExperimentSetup.AlgoType.SingleThreaded =>
            val engine = new SteadyStateEvolutionEngine[MyCandidate](
                factory,
                pipeline,
                evaluator,
                selection,
                setup.populationSize,
                false,
                util.Random(iter)
              )
            engine.setSingleThreaded(true)
            engine
          case PerfExperimentSetup.AlgoType.MasterSlave =>
            new SteadyStateEvolutionEngine[MyCandidate](
                factory,
                pipeline,
                evaluator,
                selection,
                setup.populationSize,
                false,
                util.Random(iter)
              )


        Iterator
          .range(0, repeats)
          .map(iter => runExperiment(algo(iter), setup))
          .toList

      .flatMap(_.sequence)