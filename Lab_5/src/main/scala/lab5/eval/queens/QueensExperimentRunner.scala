package lab5.eval.queens

import cats.Monad
import cats.effect.*
import cats.syntax.all.*
import cats.effect.syntax.all.*
import org.uncommons.watchmaker.framework.*
import lab5.evoalgo.*
import lab5.eval.queens.QueensResult
import lab5.eval.ExperimentRunner
import cats.effect.kernel.Outcome.Canceled
import cats.effect.kernel.Outcome.Errored
import java.util.concurrent.CancellationException

import scala.jdk.CollectionConverters.*
import lab5.Main.evaluator
import java.util.Random
import org.uncommons.watchmaker.framework.operators.*
import org.uncommons.watchmaker.framework.selection.*
import org.uncommons.maths.random.Probability
import lab5.eval.CombinedTerminationCondition
import org.uncommons.watchmaker.framework.termination.GenerationCount
import org.uncommons.watchmaker.framework.termination.TargetFitness
import java.util.concurrent.atomic.AtomicInteger
import com.google.common.util.concurrent.AtomicDouble
import cats.evidence.As
import lab5.Main.factory
import lab5.eval.queens.QueensSetup.EngineType
import lab5.util.Racing.*

case class QueensExperimentRunner[F[_]: Async: Monad: Clock](
  evaluator: QueensFitnessFunction,
  repeats: Int
)(using random: Random) extends ExperimentRunner[F, QueensSetup, List[QueensResult]]:

  private def runExperiment(
    algorithm: AbstractEvolutionEngine[QueensSolution],
    terminate: TerminationCondition,
    populationSize: Int
  ): F[QueensResult] = 

    val bestFit = AtomicDouble(Double.MaxValue)
    val firstAchievedIter = AtomicInteger(0)
    val lastGeneration = AtomicInteger(0)

    val observer: EvolutionObserver[QueensSolution] = 
      (populationData: PopulationData[_ <: QueensSolution]) =>
        val currentFit = populationData.getBestCandidateFitness()
        lastGeneration.set(populationData.getGenerationNumber())
        if currentFit < bestFit.get() then
          bestFit.set(currentFit)
          firstAchievedIter.set(populationData.getGenerationNumber())

    algorithm.addEvolutionObserver(observer)

    Clock[F]
      .timed:
        Async[F] delay algorithm.evolve(populationSize, 1, terminate)
      .map:
        case (duration, solution) => 
          QueensResult(
            bestFit.get(),
            solution,
            firstAchievedIter.get(),
            lastGeneration.get(),
            duration.toMillis
          )
    

  override def run(setup: QueensSetup): F[List[QueensResult]] = 
    Async[F]
      .delay:
        val factory = QueensFactory(setup.dimension)

        val operators: List[EvolutionaryOperator[QueensSolution]] = 
          QueensCrossover(setup.crossoverMaxLenProc, setup.crossoverProb) :: 
            setup.mutations.map:
              case (mutationType, mutationProb) => 
                QueensMutation(mutationType, mutationProb)

        val pipeline = EvolutionPipeline(operators.asJava)

        val selection: SelectionStrategy[Any] = setup.selectionType match
          case QueensSetup.SelectionType.RouletteWheel => RouletteWheelSelection()
          case QueensSetup.SelectionType.Tournament(prob) => TournamentSelection(Probability(prob))
          case QueensSetup.SelectionType.StochasticUniversalSampling => StochasticUniversalSampling()
          case QueensSetup.SelectionType.Rank => RankSelection()
          case QueensSetup.SelectionType.Truncation(ratio) => TruncationSelection(ratio)

        val algorithm = setup.engineType match
          case EngineType.SteadyState => 
            SteadyStateEvolutionEngine(
              factory,
              pipeline,
              evaluator,
              selection,
              setup.populationSize,
              false,
              random
            )
          case EngineType.Generational =>
            GenerationalEvolutionEngine(
              factory,
              pipeline,
              evaluator,
              selection,
              random
            )
      
        algorithm.setSingleThreaded(true)
        
        val terminate = CombinedTerminationCondition(
          CombinedTerminationCondition.CombineType.OR,
          GenerationCount(setup.generations + 1),
          TargetFitness(0.0, false)
        )

        (algorithm, terminate, setup.populationSize)
      .flatMap: params =>
        Iterator
          .fill(setup.repeats)(params)
          .map(runExperiment.tupled)
          .toList
          .raceMany
        

