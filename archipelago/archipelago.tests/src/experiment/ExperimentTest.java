package experiment;

import communication.Environment;
import communication.PeerAgent;
import communication.peer.AgentFactory;
import communication.peer.CompletionListeningAgent;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import learning.LabeledSample;
import learning.metrics.PerformanceMetrics;
import org.junit.Before;
import org.junit.Test;
import privacy.Budget;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by alex on 3/17/15.
 */
public class ExperimentTest {


    private AgentFactory _agentFactory;
    private List<LabeledSample> _samples;
    private int _peerCount = 2;
    private double _trainRatio;
    private List<LabeledSample> _train;
    private List<LabeledSample> _test;
    private List<List<LabeledSample>> parts;
    private PerformanceMetrics _performanceMetrics;
    private double _testCost = 0.1;
    private int _iterations;
    private Environment _environment;
    private int iterations = 5;
    private DataLoader _dataLoader;
    private double _budget;

    @Before
    public void setUp(){
        _samples = mock(List.class);
        _agentFactory = mock(AgentFactory.class);
        _performanceMetrics = mock(PerformanceMetrics.class);
        _environment = mock(Environment.class);
        _dataLoader = mock(DataLoader.class);
        _budget = 2.0;
        _trainRatio = 0.8;
        _iterations = 5;

        _train = mock(List.class);
        _test = mock(List.class);
        parts = Arrays.asList(_train, _test);
        when(_dataLoader.partition(eq(_trainRatio), any(List.class))).thenReturn(parts);
    }

    @Test
    public void construct_CreatesCorrectPeerCountAndGivesIterations() throws StaleProxyException {
        int peerCount = 3;

        new Experiment(_samples, _trainRatio, peerCount, _agentFactory, _performanceMetrics, _testCost, _environment, iterations, _dataLoader, _budget);

        verify(_agentFactory).createPeers(any(List.class), eq(peerCount), eq(_iterations), eq(_budget));
    }

    @Test
    public void construct_registersPeerWithRunEnvironment() throws StaleProxyException {
        PeerAgent agent1 = mock(PeerAgent.class);
        PeerAgent agent2 = mock(PeerAgent.class);
        when(_agentFactory.createPeers(_train, _peerCount, _iterations, _budget)).thenReturn(Arrays.asList(agent1, agent2));

        new Experiment(_samples, _trainRatio, _peerCount, _agentFactory, _performanceMetrics, _testCost, _environment, iterations, _dataLoader, _budget);

        verify(_environment).registerAgent(agent1);
        verify(_environment).registerAgent(agent2);
    }

    @Test
    public void construct_GivesPeersTrainingData() throws StaleProxyException {
        new Experiment(_samples, _trainRatio, _peerCount, _agentFactory, _performanceMetrics, _testCost, _environment, iterations, _dataLoader, _budget);

        verify(_agentFactory).createPeers(_train, _peerCount, _iterations, _budget);
    }


    @Test
    public void run_RunsEnvironmentAndRegistersCompletionAgent() throws ControllerException {
        int iterations = 10;
        int peerCount = 2;
        PeerAgent agent1 = mock(PeerAgent.class);
        PeerAgent agent2 = mock(PeerAgent.class);
        when(_agentFactory.createPeers(_train, peerCount, iterations, _budget)).thenReturn(Arrays.asList(agent1, agent2));
        Consumer<Experiment> completionListener = mock(Consumer.class);
        CompletionListeningAgent completionAgent = mock(CompletionListeningAgent.class);
        when(_agentFactory.getCompletionAgent(same(completionListener), eq(peerCount), any(Experiment.class))).thenReturn(completionAgent);


        Experiment experiment = new Experiment(_samples, _trainRatio, _peerCount, _agentFactory, _performanceMetrics, _testCost, _environment, iterations, _dataLoader, _budget);
        experiment.run(completionListener);

        verify(_environment).registerAgent(completionAgent);
    }

    @Test
    public void test_GetsResultForEachAgent() throws StaleProxyException {
        int peers = 2;
        PeerAgent agent1 = mock(PeerAgent.class);
        PeerAgent agent2 = mock(PeerAgent.class);
        when(_agentFactory.createPeers(_train, peers, _iterations, _budget)).thenReturn(Arrays.asList(agent1, agent2));
        when(agent1.labelData(_test)).thenReturn(mock(List.class));
        when(agent2.labelData(_test)).thenReturn(mock(List.class));
        when(_performanceMetrics.errorRate(same(_test), any(List.class), eq(_testCost))).thenReturn(0.15);

        Experiment experiment = new Experiment(_samples, _trainRatio, _peerCount, _agentFactory, _performanceMetrics, _testCost, _environment, iterations, _dataLoader, _budget);
        List<Double> errors = experiment.test();

        assertEquals(errors.get(0), 0.15, 0.0001d);
        assertEquals(errors.get(1), 0.15, 0.0001d);
        assertEquals(errors.size(), 2);
    }

}
