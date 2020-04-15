package Io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;
import Data.Segment;

import Data.NestPath;

public class testhtml {
	
	public static void run(String inst_name) throws Exception {
		// TODO Auto-generated method stub
		//ArrayList<NestPath> list = read_result();
		String path1 = "submit_20190923/DatasetB/DEFEATED SEEKER_" + inst_name + ".csv";
		String path2 = "data/dataB/" + inst_name + "_mianliao.csv";
		instance inst1 = distance_test.loader(path2, path1);
		htmltest(inst1, inst_name);
	}
	
	private static void htmltest(instance inst, String inst_name) throws Exception {
		    String file = "result/test" + inst_name + ".html";
		    File f = new File(file);
	        if (!f.exists()) {
	            f.createNewFile();
	        }
	        Writer writer = new FileWriter(f, false);
	        writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n" +
	                "\n" +
	                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
	                "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
	                " \n" +
	                "<svg width=\"100%\" height=\"100%\" version=\"1.1\"\n" +
	                "xmlns=\"http://www.w3.org/2000/svg\">\n");
 	        writer.write("<rect x=\"0\" y=\"0\" width=\"" + 20000 + "\" height=\"" + 1600 + "\"  fill=\"none\" stroke=\"#000000\" stroke-width=\"1\" />\n"); 
 	        for(NestPath nest : inst.nestpath) {
 	        	writer.write("<g> <polygon points=\"");
 	        	double ax = 0;
 	        	double ay = 0;
            	for(int i = 0; i < nest.getSegments().size(); i++) {
            		Segment seg = nest.get(i);
            		if(i < nest.getSegments().size() - 1) {
            			writer.write(seg.x + "," + seg.y + " ");
            		}
            		else {
            			writer.write(seg.x + "," + seg.y + "\"");
            		}
            		ax += seg.getX();
            		ay += seg.getY();
            	}
            	ax /= nest.size();
            	ay /= nest.size();
            	writer.write("style=\"fill:lightgrey;stroke:black;stroke-width:1\"/>\n");
            	writer.write("<text x=\""+ ax + "\" y=\"" + ay + "\" fill=\"red\" font-size = \"50\" >" + nest.bid + "  </g>" + "\n");
            }
 	       for(int i = 0; i < inst.r.length;i++) {
 	    	  writer.write("<g> <circle cx= \"" + inst.r_x[i] + "\"cy=\""+ inst.r_y[i] + "\"r = \""+ inst.r[i] + "\"stroke=\"black\"" + 
   	        		"stroke-width=\"2\" fill=\"red\" />\n");	
 	    	  writer.write("<text x=\""+ inst.r_x[i] + "\" y=\"" + inst.r_y[i] + "\" fill=\"black\" font-size = \"50\" >" + i + "  </g>" + "\n");
           }
	        writer.write("</svg>");
	        writer.close();
	}
}

