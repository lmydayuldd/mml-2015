package communication.peer;

import com.google.inject.Inject;
import communication.BehaviourFactory;
import communication.PeerAgent;
import communication.messaging.MessageFacadeFactory;
import communication.messaging.PeerGraph;
import experiment.Experiment;
import learning.EnsembleModel;
import learning.LabeledSample;
import privacy.NoisyQueryable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by alex on 3/10/15.
 */
public class AgentFactory {
    private final MessageFacadeFactory _messageFacadeFactory;
    private BehaviourFactory _behaviourFactory;
    private PeerGraph _peerGraph;

    @Inject
    public AgentFactory(BehaviourFactory behaviourFactory, MessageFacadeFactory messageFacadeFactory, PeerGraph peerGraph) {
        _behaviourFactory = behaviourFactory;
        _messageFacadeFactory = messageFacadeFactory;
        _peerGraph = peerGraph;
    }

    public List<PeerAgent> createPeers(NoisyQueryable<LabeledSample> data, int parts, int iterations) {
        List<NoisyQueryable<LabeledSample>> partitions = data.partition(parts);

        List<PeerAgent> agents = new ArrayList<>();

        for(NoisyQueryable<LabeledSample> partition : partitions){
            agents.add(new PeerAgent(partition, _behaviourFactory, new EnsembleModel(), _messageFacadeFactory, iterations, _peerGraph));
        }

        return agents;
    }

    public CompletionListeningAgent getCompletionAgent(Consumer<Experiment> completionAction, int totalPeerCount, Experiment experiment) {
        return new CompletionListeningAgent(completionAction, totalPeerCount, _behaviourFactory, _messageFacadeFactory, experiment);
    }
}
