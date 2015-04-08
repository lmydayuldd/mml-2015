package communication.messaging;

import communication.PeerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by alex on 3/26/15.
 */
public class PeerGraph {
    private static final String PEER_SERVICE = "peer";
    private static final String COMPLETION_SERVICE = "completion";

    public List<AID> getPeers(Agent agent) {
        DFAgentDescription description = createDescription(PEER_SERVICE);

        try {
            DFAgentDescription[] descriptions = DFService.search(agent, description);
            return IntStream.range(0, descriptions.length)
                    .mapToObj(i -> descriptions[i].getName())
                    .collect(Collectors.toList());

        } catch (FIPAException e) {
            throw new RuntimeException("Unable to search peer graph.", e);
        }
    }

    public AID getMonitoringAgent(Agent agent) {
        DFAgentDescription description = createDescription(COMPLETION_SERVICE);

        try {
            DFAgentDescription[] descriptions = DFService.search(agent, description);
            return IntStream.range(0, descriptions.length)
                    .mapToObj(i -> descriptions[i].getName())
                    .findFirst().get();

        } catch (FIPAException e) {
            throw new RuntimeException("Unable to search peer graph.", e);
        }
    }

    private DFAgentDescription createDescription(AID aid, String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(aid);
        ServiceDescription sd  = new ServiceDescription();
        sd.setName(serviceName);
        sd.setType( serviceName );
        dfd.addServices(sd);
        return dfd;
    }

    private DFAgentDescription createDescription(String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( serviceName );
        dfd.addServices(sd);
        return dfd;
    }

    public void join(PeerAgent peerAgent) {
        peerAgent.addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription description = createDescription(peerAgent.getAID(), PEER_SERVICE);
                try {
                    DFService.register(peerAgent, description);
                    System.out.println("Registered peer.");
                } catch (FIPAException e) {
                    throw new RuntimeException("Unable to register peer agent.", e);
                }
            }
        });
    }




}
