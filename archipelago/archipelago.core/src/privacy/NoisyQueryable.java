package privacy;

import learning.LabeledSample;
import privacy.math.NoiseGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by alex on 3/5/15.
 */
public class NoisyQueryable<T> {

    private List<T> _data;
    private Budget _agent;
    private NoiseGenerator _noiseGenerator;

    private NoisyQueryable(Budget agent, List<T> data, NoiseGenerator noiseGenerator) {
        _data = data;
        _agent = agent;
        _noiseGenerator = noiseGenerator;
    }

    //TODO: Thesa projections allow for counting the exact number of records or exporting the samples. Problem?
    public <Y> NoisyQueryable<Y> project(Function<T, Y> projection) {
        List<Y> values = newDataContainer();

        for(T record : _data){
            values.add(projection.apply(record));
        }

        return new NoisyQueryable<Y>(_agent, values, _noiseGenerator);
    }

    //TODO: Thesa projections allow for counting the exact number of records or exporting the samples. Problem?
    public <Y> NoisyQueryable<Y> projectIndexed(BiFunction<T, Integer, Y> projection) {
        List<Y> values = IntStream.range(0, _data.size())
                .mapToObj(i -> projection.apply(_data.get(i), i))
                .collect(Collectors.toList());
        return new NoisyQueryable<Y>(_agent, values, _noiseGenerator);
    }

    private <NewType> List<NewType> newDataContainer() {
        return new ArrayList<NewType>();
    }

    public Budget getAgent() {
        return _agent;
    }

    public double count(double epsilon) {
        checkAgentBudget(epsilon);

        _agent.apply(epsilon);
        return ((double)_data.size()) + _noiseGenerator.fromLaplacian(1.0 / epsilon);
    }

    public double noisyAverage(double cost, BiFunction<T, Integer, Double> projection) {
        checkAgentBudget(cost);
        double tally =  IntStream.range(0, _data.size())
                .mapToDouble(i -> projection.apply(_data.get(i), i))
                .map(NoisyQueryable::clamp)
                .sum();
        double count = _data.size();

        if(count == 0){
            return _noiseGenerator.uniform(-1, 1); //TODO: double check that this should be uniformly -1 and 1
        }

        double candidate = (tally + _noiseGenerator.fromLaplacian(2.0 / cost)) / count;
        while(candidate < -1.0 || candidate > 1.0){
            candidate = (tally + _noiseGenerator.fromLaplacian(2.0 / cost)) / count;
        }

        return candidate;
    }

    /**
     * Clamps output values of projection to [-1.0, +1.0]
     * @param epsilon
     * @param projection
     * @return
     */
    public double sum(double epsilon, Function<T, Double> projection) {
        checkAgentBudget(epsilon);
        return _data.stream().mapToDouble(record -> projection.apply(record))
                .map(NoisyQueryable::clamp)
                .sum() + _noiseGenerator.fromLaplacian(1.0 / epsilon);
    }

    private static double clamp(double value) {
        if(value < -1) { value = -1; }
        if(value > 1) { value = 1; }
        return value;
    }

    private void checkAgentBudget(double epsilon) {
        if(_agent.getEpsilon() < epsilon){ throw new IllegalStateException("Agent disclosure budget too low for query."); }
    }

    public List<NoisyQueryable<LabeledSample>> partition(int parts) {
        throw new UnsupportedOperationException();
    }

    public List<NoisyQueryable<LabeledSample>> partition(double trainRatio) {
        return null;
    }



}
