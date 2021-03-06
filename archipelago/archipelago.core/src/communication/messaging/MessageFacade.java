package communication.messaging;

import communication.PeerAgent;
import communication.peer.ArchipelagoPerformatives;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import learning.ParametricModel;
import privacy.math.RandomGenerator;

import java.util.List;

/**
 * Created by alex on 3/9/15.
 */
public class MessageFacade {
    private final Agent _agent;
    private ACLMessageParser _messageParser;
    private PeerGraph _peerGraph;
    private RandomGenerator _randomGenerator;


    public MessageFacade(Agent agent, ACLMessageParser messageParser, PeerGraph peerGraph, RandomGenerator randomGenerator) {
        _agent = agent;
        _messageParser = messageParser;
        _peerGraph = peerGraph;
        _randomGenerator = randomGenerator;
    }

    public boolean hasMessage(ArchipelagoPerformatives performative) {
        return hasMessage(MessageTemplate.MatchPerformative(performative.ordinal()));
    }

    public boolean hasMessage(int performative, AID sender) {
        return hasMessage(MessageTemplate.and(MessageTemplate.MatchPerformative(performative), MessageTemplate.MatchSender(sender)));
    }

    public boolean hasMessage(ArchipelagoPerformatives performative, AID sender, String conversationId) {
        return hasMessage(MessageTemplate.and(MessageTemplate.MatchPerformative(performative.ordinal()),
                MessageTemplate.and(MessageTemplate.MatchSender(sender), MessageTemplate.MatchConversationId(conversationId))));
    }


    public Boolean hasMessage(ArchipelagoPerformatives performative, String conversationId) {
        return hasMessage(MessageTemplate.and(MessageTemplate.MatchPerformative(performative.ordinal()), MessageTemplate.MatchConversationId(conversationId)));
    }


    private boolean hasMessage(MessageTemplate template) {
        ACLMessage message = _agent.receive(template);

        boolean hasMessage = message != null;
        if(hasMessage) {
            _agent.putBack(message);
        }

        return hasMessage;
    }

    public Message nextMessage(ArchipelagoPerformatives performative, AID sender, String conversationId) {
        return nextMessage(MessageTemplate.and(
                MessageTemplate.and(MessageTemplate.MatchSender(sender), MessageTemplate.MatchConversationId(conversationId)),
                MessageTemplate.MatchPerformative(performative.ordinal())));
    }

    public Message nextMessage(ArchipelagoPerformatives performative) {
        return nextMessage(MessageTemplate.MatchPerformative(performative.ordinal()));
    }

    public Message nextMessage(ArchipelagoPerformatives performative, String conversationId) {
        return nextMessage(MessageTemplate.and(MessageTemplate.MatchPerformative(performative.ordinal()), MessageTemplate.MatchConversationId(conversationId)));
    }

    public Message nextMessage(MessageTemplate template){
        if(!hasMessage(template)){ throw new IllegalStateException("No messages available, but nextMessage was called."); }

        ACLMessage message = _agent.receive(template);
        Message parsedMessage;
        try {
            parsedMessage = _messageParser.parse(message);
        } catch (OntologyException e) {
            throw new RuntimeException("Error when parsing message");
        }

        return parsedMessage;
    }

    public void sendToRandomPeer(ParametricModel model) {
        List<AID> peers = _peerGraph.getPeers(_agent);

        if(peers.size() == 0)
            return;

        AID target = peers.get(_randomGenerator.uniform(0, peers.size() - 1));
        ACLMessage message = _messageParser.createModelMessage(model, target);

        _agent.send(message);
    }

    public void publishAggregationGroup(List<AID> group, String conversationId) {
        ACLMessage message = _messageParser.createGroupMessage(group, conversationId);
        for (AID aid : group) {
            message.addReceiver(aid);
        }
        _agent.send(message);
    }

    public GroupMessage nextAggregationGroupMessage() {
        if(!hasMessage(ArchipelagoPerformatives.GroupFormation)) {
            throw new IllegalStateException("Requested a group message when it is not available.");
        }

        ACLMessage aclMessage = _agent.receive(MessageTemplate.MatchPerformative(ArchipelagoPerformatives.GroupFormation.ordinal()));
        return _messageParser.parseGroupMessage(aclMessage);
    }

    public void sendToPeer(AID receiver, ParametricModel model, ArchipelagoPerformatives performative, String conversationId, int datasetSize) {
        ACLMessage message = _messageParser.createModelMessage(model, performative, datasetSize);
        message.addReceiver(receiver);
        message.setConversationId(conversationId);

        _agent.send(message);
    }

    public void sendToPeer(AID receiver, ParametricModel model, ArchipelagoPerformatives performative, String conversationId) {
        ACLMessage message = _messageParser.createModelMessage(model, performative);
        message.addReceiver(receiver);
        message.setConversationId(conversationId);

        _agent.send(message);
    }

    public void sendCompletionMessage(AID agent1) {
        ACLMessage message = _messageParser.createCompletionMessage(agent1);
        message.addReceiver(_peerGraph.getMonitoringAgent(_agent));

        _agent.send(message);
    }

    public void sendCompletionMessage(String conversationId) {
        ACLMessage message = _messageParser.createCompletionMessage(conversationId);
        message.addReceiver(_peerGraph.getMonitoringAgent(_agent));

        _agent.send(message);
    }

    public void sendToAll(ParametricModel mergedModel, ArchipelagoPerformatives performative) {
        ACLMessage message = _messageParser.createModelMessage(mergedModel, performative);
        List<AID> peers = _peerGraph.getPeers(_agent);
        for (AID peer : peers) {
            message.addReceiver(peer);
        }

        _agent.send(message);
    }

    public void sendToAllExcept(ParametricModel mergedModel, ArchipelagoPerformatives performative, Agent agent) {
        ACLMessage message = _messageParser.createModelMessage(mergedModel, performative);
        List<AID> peers = _peerGraph.getPeers(_agent);
        if(peers.contains(agent.getAID())) {
            peers.remove(agent.getAID());
        }
        for (AID peer : peers) {
            message.addReceiver(peer);
        }

        _agent.send(message);
    }
}
