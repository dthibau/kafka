package org.formation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckLog {

	public static void main(String[] args) throws URISyntaxException {
		
		try {

            File f = new File(args[0]);

            BufferedReader b = new BufferedReader(new FileReader(f));

            String readLine = "";

            Map<Integer,List<Integer>> keyValues = new HashMap<>();
            while ((readLine = b.readLine()) != null) {
            	if ( readLine.indexOf("DEBUG") != -1 && 
            			readLine.indexOf("org.formation.Listener") != -1) {
                	try {
                		String info = readLine.substring(101);
                        String [] data = info.split("/");
                        int key =Integer.parseInt(data[0].trim());
                        List<Integer> offsets = keyValues.get(key);
                        if ( offsets == null ) {
                        	offsets = new ArrayList<>();
                        	keyValues.put(key, offsets);
                        }
                        offsets.add(Integer.parseInt(data[1].trim()));
                    	} catch (Exception e) {
                    		System.err.println(readLine + " - " + e);
                    	}
            		
            	}

            }
            b.close();
            
            Map<Integer,Integer> minOffsets = new HashMap<>();
            Map<Integer,Integer> maxOffsets = new HashMap<>();
            for (int key : keyValues.keySet()) {
            	minOffsets.put(key, Collections.min(keyValues.get(key)));
            	maxOffsets.put(key, Collections.max(keyValues.get(key)));
            }
            
            List<String> missed = new ArrayList<>();
            List<String> doublon = new ArrayList<>();
            for (int key : keyValues.keySet()) {
            	for ( int i = minOffsets.get(key); i< maxOffsets.get(key); i++) {
            		int count = 0;
            		for ( int offset : keyValues.get(key) ) {
            			if ( offset == i) {
            				count++;
            			}
            		}
            		if ( count == 0) {
            			missed.add(key +"/"+i);
            		} else if ( count > 1) {
            			doublon.add(key +"/"+i);
            		}
            	}
            }
            System.out.println("Min offset : "+minOffsets);
            System.out.println("Max offset : "+maxOffsets);

            System.out.println("Missed offset : "+missed);
            System.out.println("Doublon offset : "+doublon);
            

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
