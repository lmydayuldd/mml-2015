package experiment;

import communication.Environment;
import communication.messaging.PeerGraph;
import communication.peer.AgentFactory;
import jade.wrapper.ControllerException;
import learning.LabeledSample;
import learning.metrics.PerformanceMetrics;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by aspis on 25.03.2015.
 */
public class ExperimentFactoryTest {

    private ExperimentFactory _experimentFactory;
    private AgentFactory _peerFactory;
    private PerformanceMetrics _performanceMetrics;
    private Environment _environment;
    private DataLoader _dataLoader;
    private int _parameters = 30;
    private double _updateCost = 2.0;
    private PeerGraph _peerGraph;

    @Before
    public void setUp() {

        _peerGraph = mock(PeerGraph.class);
        _peerFactory = mock(AgentFactory.class);
        _performanceMetrics = mock(PerformanceMetrics.class);
        _environment = mock(Environment.class);
        _dataLoader = mock(DataLoader.class);

        _experimentFactory = new ExperimentFactory(_peerFactory, _performanceMetrics, _environment, _dataLoader, _peerGraph);
    }

    @Test
    public void getExperiment_ConfiguresExperiment() throws ControllerException {
        List<LabeledSample> samples = mock(List.class);
        when(_dataLoader.partition(anyDouble(), same(samples))).thenReturn(Arrays.asList(mock(List.class), mock(List.class)));
        double trainRatio = 0.5;
        int peerCount = 10;
        double testCost = 0.1;
        int iterations = 10;
        double regularization = 1.0;
        int groupSize = 3;
        int recordsPerPeer = 100;

        ExperimentConfiguration configuration = new ExperimentConfiguration(iterations, 0.0, trainRatio, peerCount, testCost, _updateCost, regularization, groupSize, recordsPerPeer);

        Experiment experiment = _experimentFactory.getExperiment(samples, testData, configuration);

        assertEquals(samples, experiment.getData());
        assertEquals(trainRatio, experiment.getConfiguration().trainRatio, 0.0001d);
        assertEquals(peerCount, experiment.getConfiguration().peerCount);
        assertEquals(testCost, experiment.getConfiguration().testCost, 0.0001d);
        assertEquals(iterations, experiment.getConfiguration().aggregations);
        assertEquals(_updateCost, experiment.getConfiguration().epsilon, 0.0001d);
        assertEquals(regularization, experiment.getConfiguration().regularization, 0.0001d);
        assertEquals(groupSize, experiment.getConfiguration().groupSize);
    }

}
