package org.maas;

import java.util.Vector;
import org.maas.Initializer;

public class VisualisationInitializer extends Initializer {
    @Override
    public String initialize(String scenarioDirectory) {
        Vector<String> agents = new Vector<>();

        agents.add("board-visualisation:org.right_brothers.agents.VisualizationAgent");

        String agentInitString = String.join(";", agents);
        agentInitString += ";";
        return agentInitString;
    }
}
