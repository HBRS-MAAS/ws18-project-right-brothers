package org.right_brothers.agents;

import jade.core.Agent;

import java.util.*;
import java.util.stream.Collectors;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.right_brothers.objects.Order;
import org.right_brothers.objects.Date;
import org.right_brothers.objects.ScenarioParser;
import org.right_brothers.objects.Location;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

@SuppressWarnings("serial")
public class BakeryCustomerAgent extends Agent {
    private AID[] sellerAgents;

    private static int totalAgents;
    private static ScenarioParser sp;
    private String name;
    private String guid;
    private int type;
    private Location location;
    private ArrayList orders;

    protected void setup() {
        System.out.println("\tCustomer-agent "+getAID().getLocalName()+" is born.");
        totalAgents++;

        this.getCustomerInformation();
        
        this.publishCustomerAID();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Order order = new Order("order-001");
        
        this.getOrderProcessorAID();
        Object[] args = getArguments();
        
        addBehaviour(new  RequestPerformer(order));
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    protected void publishCustomerAID(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Bakery-customer-agent");
        sd.setName("JADE-bakery");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    protected void getOrderProcessorAID() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Order-processing-agent");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            sellerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                sellerAgents[i] = result[i].getName();
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    public static void setScenarioParser(ScenarioParser sp_object){
        sp = sp_object;
    }
    private void getCustomerInformation(){
        JSONObject my_info = this.sp.get_customer_from_guid(getAID().getLocalName());
        // System.out.println(my_info);
        this.name = (String) my_info.get("name");
        this.guid = (String) my_info.get("guid");
        this.type = (int)(long) my_info.get("type");
        JSONObject loc = (JSONObject) my_info.get("location");
        this.location = new Location((float)(double) loc.get("x"), (float)(double) loc.get("y"));
        JSONArray orders = (JSONArray) my_info.get("orders");
        this.orders = new ArrayList();

        Iterator<JSONObject> i = orders.iterator();
        while (i.hasNext()) {
            JSONObject jsonOrder = (JSONObject) i.next();
            guid = (String) jsonOrder.get("guid");
            Order order = new Order(guid);
            JSONObject date = (JSONObject) jsonOrder.get("orderDate");
            Date order_date = new Date((int)(long) date.get("day"), (int)(long) date.get("hour"));
            order.setOrderDate(order_date);
            date = (JSONObject) jsonOrder.get("deliveryDate");
            Date delivery_date = new Date((int)(long) date.get("day"), (int)(long) date.get("hour"));
            order.setOrderDate(delivery_date);
            String products = (String) jsonOrder.get("products").toString();
            order.setProducts(products);
            String customer_id = (String) jsonOrder.get("customer_id");
            order.setCustomerId(customer_id);
            this.orders.add(order);
        }
    }

    /*
     * Inner class RequestPerformer.
     * This is the behavior used by Bread-buyer agents to request seller
     * agents the target Bread.
     * */
	private class RequestPerformer extends Behaviour {
		private MessageTemplate mt;
		private int step = 0;
    private Order order;

    RequestPerformer (Order order) {
        this.order = order;
    }

		public void action() {
			switch (step) {
			case 0:
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < sellerAgents.length; ++i) {
					cfp.addReceiver(sellerAgents[i]);
				}
        try {
            cfp.setContentObject(this.order);

        } catch(Exception e){
            e.printStackTrace();
        }
				cfp.setConversationId("Bread-trade");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Bread-trade"),
				MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.CONFIRM) {
                        System.out.println("\t" + myAgent.getLocalName() + " received confirmation from " + reply.getSender().getLocalName());
                        totalAgents--;
						step = 2;
					}
				}
				else {
					block();
				}
				break;
            default:
                break;
			}
		}
		public boolean done() {
            if (step == 2) {
                System.out.println(totalAgents);
            	if(totalAgents == 0) {
                    myAgent.addBehaviour(new shutdown());
            	}
                else {
                    myAgent.doDelete();
                }
                return true;
            }
            return false;
		}
		
	} // End of inner class RequestPerformer

    // Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
    private class shutdown extends OneShotBehaviour{
		public void action() {
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
