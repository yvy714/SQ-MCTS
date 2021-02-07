
import goalplantree.*;
import simulation.Simulator;
import xml2bdi.XMLReader;
import environment.*;

import java.util.ArrayList;
import agent.*;

public class Main {

    public static void main(String[] args){


        double total = 0;
        int type;
        int testNum;

        String gptFilePath;
        XMLReader reader;

        if (args.length == 0) {
            System.out.println("ERROR: no GPT file specified!");
            return;
        }
        gptFilePath = args[0];


        // read the type
        try {
            type = Integer.parseInt(args[1]);
            if(type < 2 || type > 5){
                type = 2;
            }
        }catch (Exception e){
            type = 0;
        }
        // read the test num
        try{
            testNum = Integer.parseInt(args[2]);
        }catch (Exception e){
            testNum = 10;
        }

        System.out.println("type: " +  type);

        Simulator simulator = new Simulator();

        for(int m = 0; m < testNum; m++){

            try {
                reader = new XMLReader(gptFilePath);
            } catch(Exception e) {
                System.out.println("ERROR: unable to open GPT file: " + gptFilePath);
                return;
            }


            // get the list of literals in the environment
            ArrayList<Literal> literals = reader.getLiterals();
            // get the list of goals
            ArrayList<GoalNode> tlgs = reader.getTlgs();


            if(type == 5) {
                for (int i = 0; i < tlgs.size(); i++) {
                    simulator.runSimulation(10000, tlgs.get(i));
                    System.out.println("sim:" + i);
                }
            }


            // build the environment
            SynthEnvironment environment = new SynthEnvironment(literals, 0);
            System.out.println(environment.onPrint());
            System.out.println("--------------------------------------------------------");


            // build the agent
            ArrayList<Belief> bs = new ArrayList<>();
            for(Literal l : literals){
                    bs.add(new Belief(l.getName(), l.getState() ? 1 : 0));
            }

            // build the fifo agent
            MCTSAgent mctsAgent = new MCTSAgent("MCTS-Agent", bs, tlgs);
            SPMCTSAgent spmctsAgent = new SPMCTSAgent("SPMCTS-Agent", bs, tlgs);
            QSISPMCTSAgent qsispmctsAgent = new QSISPMCTSAgent("QSISPMCTS-Agent", bs, tlgs);


            AbstractAgent agent = null;

            System.out.println("type:" + type);
            switch (type){
                case 2: agent = mctsAgent;break;
                case 3: agent = spmctsAgent;break;
                case 5: agent = qsispmctsAgent; break;
            }

            // add this agent to the environment
            environment.addAgent(agent);

            boolean running = true;

            int step = 1;
            while (running){
                System.out.println("---------------------step " + step + "------------------------------");
                running = environment.run();
                step++;
            }
            // check the number of goals achieved
            //System.out.println(agent.getNumAchivedGoal());
            total += agent.getNumAchivedGoal();
        }

    }

}
