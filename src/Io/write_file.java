package Io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import Data.NestPath;
import Data.Placement;
import Io.instance;
import Util.GeometryUtil;

public class write_file {
	//保存为cvs文件
	public static void save_result(List<Placement> applyplace, List<NestPath> parts ,instance inst) throws IOException{
		 DecimalFormat df1 = new DecimalFormat("#.00000");
		 DecimalFormat df2 = new DecimalFormat("#");
		 String file = "submit_20190923/DatasetB/"+"DEFEATED SEEKER_"+ inst.pid + ".csv";   //+ "_" + df2.format(System.nanoTime() / 1e9)
		 OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
		 BufferedWriter writer = new BufferedWriter(outputWriter);
	     writer.write("下料批次" + "," + "零件" + "," + "面料" + "," + "零件外轮廓线坐标");
	     writer.newLine();
	     double ox = 0;
	     double oy = 0;
	     double rotate = 0;
	     for(int i = 0; i < parts.size(); i++) {
	      NestPath polygon = new NestPath(parts.get(i));
	      for(int p = 0; p < applyplace.size(); p++) {
	        Placement place = applyplace.get(p);
	        if(place.bid == polygon.bid) {
	               ox = place.translate.getX();
	               oy = place.translate.getY();
	               rotate = place.rotate;
	               break;
	        }
	       }
	      writer.write(inst.pid);
	      writer.write(",");
	      writer.write("s" + String.format("%06d",polygon.bid));
	      writer.write(",");
	      writer.write(inst.mid);
	      writer.write(",");
	      writer.write("\"[");
	      if(rotate != 0) {
	       polygon = GeometryUtil.rotatePolygon2Polygon(polygon, 180);
	      }
	      for(int j = 0; j < polygon.size(); j++) {
	       double nx = 0;
	       double ny = 0;
	       nx = ox + polygon.get(j).x + inst.minside/2;
	       ny = oy + polygon.get(j).y + inst.minside/2;

	       if(j < polygon.size() - 1) {
	        writer.write("[" + df1.format(nx) + ", " + df1.format(ny) + "], ");
	       }
	       else
	        writer.write("[" + df1.format(nx) + ", " + df1.format(ny) + "]");
	      }
	      writer.write("]\"");
	      writer.newLine();
	     }
	     writer.close();
	 }
	
	//结果展示
		public static void saveSvgFile(List<String> strings) throws Exception {
	        File f = new File("result.html");
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
	        for(String s : strings){
	            writer.write(s);
	        }
	        writer.write("</svg>");
	        writer.close();
	    }
}
