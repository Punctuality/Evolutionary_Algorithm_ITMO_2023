package additional1.eval;

import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.Arrays;
import java.util.List;

public class CombinedTerminationCondition implements TerminationCondition {

    public enum CombineType {
        AND,
        OR
    }

    final List<TerminationCondition> conditionList;
    final CombineType combineType;

    public CombinedTerminationCondition(CombineType combineType, TerminationCondition... conditionList){
        this.conditionList = Arrays.stream(conditionList).toList();
        this.combineType = combineType;
    }

    long countTerminated(PopulationData<?> populationData) {
        return conditionList.stream().filter(cond -> cond.shouldTerminate(populationData)).count();
    }

    @Override
    public boolean shouldTerminate(PopulationData<?> populationData) {
        switch (combineType) {
            case OR -> {
                return countTerminated(populationData) > 0;
            }
            case AND -> {
                return countTerminated(populationData) == conditionList.size();
            }
        }
        return false;
    }
}
