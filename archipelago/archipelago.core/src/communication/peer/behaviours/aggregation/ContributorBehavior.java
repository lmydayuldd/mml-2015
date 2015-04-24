package communication.peer.behaviours.aggregation;

import communication.PeerAgent;
import communication.messaging.Message;
import communication.messaging.MessageFacade;
import communication.peer.AggregationPerformative;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;

/**
 * Created by alex on 4/22/15.
 */
public class ContributorBehavior extends CyclicBehaviour{
    private final PeerAgent _peerAgent;
    private final AID _curator;
    private boolean modelSent;
    private String _conversationId;
    private MessageFacade _messageFacade;

    public ContributorBehavior(PeerAgent peerAgent, AID curator, String conversationId, MessageFacade messageFacade) {
        _peerAgent = peerAgent;
        _curator = curator;
        _conversationId = conversationId;
        _messageFacade = messageFacade;
        modelSent = false;
    }

    @Override
    public void action() {

        if (_messageFacade.hasMessage(AggregationPerformative.AggregatedResult.ordinal(), _curator, _conversationId)) {
            Message message = _messageFacade.nextMessage(AggregationPerformative.AggregatedResult.ordinal(), _curator, _conversationId);
            _peerAgent.addModel(message.getModel());
            _peerAgent.removeBehaviour(this);
        }
        else if (!modelSent) {
            _messageFacade.sendToPeer(_curator, _peerAgent.getLocalModel(), AggregationPerformative.ModelContribution, _conversationId);
            modelSent = true;
        }

    }
}
