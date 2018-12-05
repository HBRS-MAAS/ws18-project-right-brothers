package org.right_brothers.agents;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

// for shutdown behaviour
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.FIPANames;

import org.right_brothers.utils.Time;

@SuppressWarnings("serial")
public class TimeKeeper extends Agent{
	private int currentTimeStep;
    private Time currentTime;
    //TODO: read singleTimeStep from meta.json
    private Time singleTimeStep = new Time(0,1,0);
	private int countAgentsReplied;
    private Time endTime;
    private List<AID> finishedAgents;
	
	protected void setup() {
		System.out.println("\tHello! time-keeper-agent "+getAID().getLocalName()+" is ready.");
		
        this.currentTime = new Time();
        finishedAgents = new Vector<AID> ();

        /* Wait for all the agents to start
         */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String firstArgument = (String) args[0];
            // if end time is given in number of timeSteps (int)
            if (firstArgument.matches("\\d+")){
                int totalTimeSteps = Integer.parseInt((String) args[0]);
                this.endTime = new Time(totalTimeSteps, this.singleTimeStep);
            }
            // end time is given in TimeString format ("ddd.hh.mm")
            else{
                this.endTime = new Time((String) args[0]);
            }
        }else {
            endTime = new Time(0,12,0);
        }

		addBehaviour(new SendTimeStep());
		addBehaviour(new TimeStepConfirmationBehaviour());
	}
	
	protected void takeDown() {
        System.out.println("\t" + this.getAID().getLocalName() + " terminating.");
	}
	
    /* Get the AID for all alive agents who have registered with "JADE-bakery" name
     */
	private List<DFAgentDescription> getAllAgents(){
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
        sd.setName("JADE-bakery");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			return Arrays.asList(result);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
			return new Vector<DFAgentDescription>();
		}
	}
	
    /* Send next time step to all agents so that they can proceed with their tasks
     */
	private class SendTimeStep extends OneShotBehaviour {
		public void action() {
            List<DFAgentDescription> agents = getAllAgents();
            currentTime.add(singleTimeStep);
            if (currentTime.greaterThan(endTime)) {
                myAgent.addBehaviour(new shutdown());
                return;
            }
            countAgentsReplied = agents.size();
            finishedAgents.clear();
            System.out.println(">>>>> " + currentTime + " <<<<<");
            for (DFAgentDescription agent : agents) {
                ACLMessage timeMessage = new ACLMessage(55);
                timeMessage.addReceiver(agent.getName());
                timeMessage.setContent(currentTime.toString());
                myAgent.send(timeMessage);
            } 
		}
	}
	
    /* Get `finish` message from all agents (BaseAgent) and once all message are received
     * call SendTimeStep to increment time step
     */
	private class TimeStepConfirmationBehaviour extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
                AID agent = msg.getSender();
                if (!finishedAgents.contains(agent)){
                    finishedAgents.add(agent);
                    countAgentsReplied--;
                    if (countAgentsReplied == 0){
                        myAgent.addBehaviour(new SendTimeStep());
                        block();
                    }
                }
			}
			else {
				block();
			}
		}
	}
    
    // Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
    private class shutdown extends OneShotBehaviour{
        public void action() {
            System.out.println("Simulation ended. Shutting down platform.");
            ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
            Codec codec = new SLCodec();
            myAgent.getContentManager().registerLanguage(codec);
            myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
            shutdownMessage.addReceiver(myAgent.getAMS());
            shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
            try {
                myAgent.getContentManager().fillContent(shutdownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
                myAgent.send(shutdownMessage);
            }
            catch (Exception e) {}
        }
    }
}
