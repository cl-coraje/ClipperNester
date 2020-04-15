package Io;

import java.util.ArrayList;
import java.util.List;

import Data.Bound;
import Data.NestPath;
import Data.Segment;
import Util.GeometryUtil;

public class polygon_test {
	
	public static void run(List<NestPath> parts) {
		List<NestPath> copy_parts = new ArrayList<NestPath>();
		for(int i = 0; i < parts.size(); i++) {
			NestPath copy_path = new NestPath(parts.get(i));
			copy_parts.add(copy_path);
		}
		//List<NestPath> tparts = Sort_coordinate(copy_parts);
		same_test(copy_parts);	
		
		for(int i = 0; i < copy_parts.size(); i++) {
			parts.get(i).type = copy_parts.get(i).type;
		}
		
		System.out.println("共有" + parts.get(parts.size() - 1).type + "种类型的零件");
	}
       public static void same_test(List<NestPath> parts) {
    	   int type = 1;
    	   for(int i = 0; i < parts.size(); i++) {
    		   NestPath p1 = parts.get(i);
    		   if(p1.type == 0) {
    			   p1.type = type;
    			   type++;
    		   }	   
    		   for(int j = i + 1; j < parts.size(); j++) {
    			   boolean is_same = false;
    			   NestPath p2 = parts.get(j);
    			   int t = 0;
    			   if(p1.size() == p2.size() && almostEqual(GeometryUtil.polygonArea(p1), GeometryUtil.polygonArea(p2))) {
    				   for(t = 0; t < p2.size(); t++) {
    					   Segment s1 = p1.get(t);
    					   Segment s2 = p2.get(t);
    					   if(!almostEqual(s1.getX(), s2.getX()) || !almostEqual(s1.getY(), s2.getY())) {
    						   break;
    					   }
    				   }
    				   if(t == p2.size()) {
    					   is_same = true;
    					   if(p1.type != 0 && p2.type == 0) {
    						   p2.type = p1.type;
    					   }
    					   if(p1.type != 0 && p2.type != 0) {   
    						   if(p1.type != p2.type) {
    							   System.out.println("Match Error------" + p1.bid + "\t" + p2.bid);
    							   System.exit(0);
    						   }
    					   }
    				   }
    			   }
    			   
    	     }
    	   }
       }
       
       public static boolean almostEqual(double a, double b ){
           return Math.abs(a-b) < 0.001;
       }
       
       //将polygon转化为以x轴最小坐标为参照点的相对坐标点
       public static List<NestPath> Sort_coordinate(List<NestPath> parts){
    	   List<NestPath> tparts = new ArrayList<NestPath>();
    	   for(int i = 0; i < parts.size(); i++) {
    		   NestPath path = parts.get(i);
    		   //确保为逆时针
    		   if(GeometryUtil.polygonArea(path) > 0 )
    			   path.reverse();
  		   
    		   //获得参照点
    		   int position = getMinimizeX(path);
    		   Segment oseg = path.get(position);
               
               //以参照点为起始点
    		   List<Segment> new_seg = new ArrayList<Segment>();
               new_seg = Order_coordinate(path, position);  
               NestPath new_path = path;
               new_path.setSegments(new_seg);
               
               List<Segment> new_seg2 = new ArrayList<Segment>();
               for(int j = 0; j < new_path.size(); j++) {
            	   Segment seg = new_path.get(j);
            	   new_seg2.add(new Segment(seg.getX() - oseg.getX(), seg.getY() - oseg.getY()));
               }
               new_path.clear();
               new_path.setSegments(new_seg2);
    		   tparts.add(new_path);
    	   }    	   
    	   return tparts;
       }
       
       //找到x坐标最小的点作为参照点
       public static int getMinimizeX(NestPath polygon) {
    	   int record = 0;
    	   double xmin = polygon.get(0).getX();
    	   double y_xmin = polygon.get(0).getY();
           for(int i = 1 ; i < polygon.size(); i++){
               double x = polygon.get(i).getX();
               double y = polygon.get(i).getY();
               if(x < xmin){
            	   xmin = x;
                   y_xmin = y;
                   record = i;
               }
               if(x == xmin) {
            	   if(y < y_xmin) {
            		   y_xmin = y;
            		   record = i;
            	   }
               }
           }
           return record;
       }
       
       //将polygon的坐标点以参照点为起始点
       public static List<Segment> Order_coordinate(NestPath path, int p){
    	   List<Segment> new_seg = new ArrayList<Segment>();
    	   for(int i = p; i < path.size(); i++) {
    		   Segment seg = path.get(i);
    		   new_seg.add(seg);
    	   }
    	   for(int i = 1; i < p; i++) {
    		   Segment seg = path.get(i);
    		   new_seg.add(seg);
    	   }
    	   new_seg.add(new Segment(path.get(0).getX(), path.get(0).getY()));
    	   return new_seg;
       }
}
