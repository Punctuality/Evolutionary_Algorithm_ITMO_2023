package additional1.eval;

import com.google.common.collect.Lists;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

public class GridEvaluator<R, E extends RecursiveTask<R>> extends RecursiveAction {

    final List<String> paramNames;
    final List<String> resultNames;
    final List<?>[] variables;
    final Function<List<?>, E> compiler;
    final Function<R, List<String>> rowLogger;
    final File outputFile;

    public GridEvaluator(
            File outputFile,
            Function<List<?>, E> compiler,
            Function<R, List<String>> rowLogger,
            List<String> paramNames,
            List<String> resultNames,
            List<?>... variables
    ) {
        this.outputFile = outputFile;
        this.paramNames = paramNames;
        this.resultNames = resultNames;
        this.variables = variables;
        this.compiler = compiler;
        this.rowLogger = rowLogger;
    }

    List<Pair<List<?>, E>> produceTasks() {
        return Lists.cartesianProduct(this.variables).stream().map(input -> {
            E task = this.compiler.apply(input);
            return new Pair<List<?>, E>(input, task);
        }).toList();
    }

    @Override
    protected void compute() {
        try {
            FileOutputStream fos = new FileOutputStream(this.outputFile);
            PrintWriter p = new PrintWriter(fos);

            List<String> allNames = new ArrayList<>(paramNames);
            allNames.addAll(resultNames);


            List<Pair<List<?>, E>> tasks = produceTasks();
            System.out.printf("Total number of configs to run: %d (doesn't account for repeats)\n", tasks.size());

            p.println(allNames.stream().reduce((s, s2) -> s + "," + s2).get());

            ForkJoinTask.invokeAll(tasks.stream().map(Pair::getValue1).toList());
            tasks
                .stream()
                .map(paramsAndTask -> {
                    List<String> paramElems = paramsAndTask.getValue0().stream().map(Object::toString).toList();
                    R result = paramsAndTask.getValue1().join();
                    return new Pair<>(result, paramElems);
                })
                .map(result -> {
                    List<String> allRowElems = new ArrayList<>(result.getValue1());
                    allRowElems.addAll(this.rowLogger.apply(result.getValue0()));
                    return allRowElems.stream().reduce((s, s2) -> s + "," + s2).get();
                }).forEachOrdered(p::println);

            p.flush();
            p.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File Not Found");
        }
    }
}
