package lab6.eval

trait ExperimentRunner[F[_], S, R]:
  def run(setup: S): F[R]