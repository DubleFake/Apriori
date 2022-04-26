package main;

import java.io.*;
import java.util.*;

public class Apriori {

    private List<int[]> itemsets ;
    private String transaFile; 
    private int numItems; 
    private int numTransactions; 
    private double minSup; 

    public Apriori(String[] args) throws Exception
    {
        configure(args);
        go();
    }

    private void go() throws Exception {
        long start = System.currentTimeMillis();

        createItemsetsOfSize1();        
        int itemsetNumber=1; 
        int nbFrequentSets=0;
        
        while (itemsets.size()>0)
        {

            calculateFrequentItemsets();

            if(itemsets.size()!=0)
            {
                nbFrequentSets+=itemsets.size();
                log("Found "+itemsets.size()+" frequent itemsets of size " + itemsetNumber + " (with support "+(minSup*100)+"%)\n");
                createNewItemsetsFromPreviousOnes();
            }

            itemsetNumber++;
        } 

        //display the execution time
        long end = System.currentTimeMillis();
        log("Execution time is: "+((double)(end-start)/1000) + " seconds.");
        log("Found "+nbFrequentSets+ " frequents sets for support "+(minSup*100)+"%)");
        log("Done");
    }


    private void foundFrequentItemSet(int[] itemset, int support) {
    		System.out.println(Arrays.toString(itemset) + "  ("+ ((support / (double) numTransactions))+" "+support+")");
    }

    private void log(String message) {
    		System.out.println(message);
    }

    private void configure(String[] args) throws Exception
    {        
        
        if (args.length!=0) transaFile = args[0];
        else throw new Exception("Please speciy a file path.");
    	
    	
    	if (args.length>=2) minSup=(Double.valueOf(args[1]).doubleValue());    	
    	else minSup = .8;// by default
    	if (minSup>1 || minSup<0) throw new Exception("minSup: bad value");
    	
    	numItems = 0;
    	numTransactions=0;
    	BufferedReader data_in = new BufferedReader(new FileReader(transaFile));
    	while (data_in.ready()) {    		
    		String line=data_in.readLine();
    		if (line.matches("\\s*")) continue;
    		numTransactions++;
    		StringTokenizer t = new StringTokenizer(line," ");
    		while (t.hasMoreTokens()) {
    			int x = Integer.parseInt(t.nextToken());
    			//log(x);
    			if (x+1>numItems) numItems=x+1;
    		}    		
    	}  
    	
    	data_in.close();
        outputConfig();

    }

	private void outputConfig() {
		//output config info to the user
		 log("Input configuration: "+numItems+" items, "+numTransactions+" transactions, minsup = "+minSup*100+"%\n");
	}

	private void createItemsetsOfSize1() {
		itemsets = new ArrayList<int[]>();
        for(int i=0; i<numItems; i++)
        {
        	int[] cand = {i};
        	itemsets.add(cand);
        }
	}
			
    private void createNewItemsetsFromPreviousOnes()
    {
    	int currentSizeOfItemsets = itemsets.get(0).length;   		
    	HashMap<String, int[]> tempCandidates = new HashMap<String, int[]>(); 
    	
        
        for(int i=0; i<itemsets.size(); i++)
        {
            for(int j=i+1; j<itemsets.size(); j++)
            {
                int[] X = itemsets.get(i);
                int[] Y = itemsets.get(j);

                assert (X.length==Y.length);
                
                
                int [] newCand = new int[currentSizeOfItemsets+1];
                for(int s=0; s<newCand.length-1; s++) {
                	newCand[s] = X[s];
                }
                    
                int ndifferent = 0;
                for(int s1=0; s1<Y.length; s1++)
                {
                	boolean found = false;
                    for(int s2=0; s2<X.length; s2++) {
                    	if (X[s2]==Y[s1]) { 
                    		found = true;
                    		break;
                    	}
                	}
                	if (!found){ 
                		ndifferent++;
                		newCand[newCand.length -1] = Y[s1];
                	}
            	
            	}
                assert(ndifferent>0);
                
                
                if (ndifferent==1) {
                	Arrays.sort(newCand);
                	tempCandidates.put(Arrays.toString(newCand),newCand);
                }
            }
        }
        
        itemsets = new ArrayList<int[]>(tempCandidates.values());
    	
    }

    private void line2booleanArray(String line, boolean[] trans) {
	    Arrays.fill(trans, false);
	    StringTokenizer stFile = new StringTokenizer(line, " "); 
	    while (stFile.hasMoreTokens())
	    {
	    	
	        int parsedVal = Integer.parseInt(stFile.nextToken());
			trans[parsedVal]=true; 
	    }
    }

    private void calculateFrequentItemsets() throws Exception
    {
    	List<int[]> frequentCandidates = new ArrayList<int[]>(); 

        boolean match; 
        int count[] = new int[itemsets.size()];


		
		BufferedReader data_in = new BufferedReader(new InputStreamReader(new FileInputStream(transaFile)));

		boolean[] trans = new boolean[numItems];
		
		
		for (int i = 0; i < numTransactions; i++) {

			
			String line = data_in.readLine();
			line2booleanArray(line, trans);

			
			for (int c = 0; c < itemsets.size(); c++) {
				match = true; 
				int[] cand = itemsets.get(c);
				for (int xx : cand) {
					if (trans[xx] == false) {
						match = false;
						break;
					}
				}
				if (match) { 
					count[c]++;
				}
			}

		}
		
		data_in.close();

		for (int i = 0; i < itemsets.size(); i++) {
			if ((count[i] / (double) (numTransactions)) >= minSup) {
				foundFrequentItemSet(itemsets.get(i),count[i]);
				frequentCandidates.add(itemsets.get(i));
			}
			
		}

        
        itemsets = frequentCandidates;
    }
}
