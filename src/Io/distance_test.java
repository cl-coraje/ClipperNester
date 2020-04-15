package Io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Data.NestPath;
import Data.Segment;
import Io.instance;
import Util.GeometryUtil;

public class distance_test {
	
	public static DecimalFormat df = new DecimalFormat("#.000000");
	
	public static void run(String inst_name) throws Exception {
//		String file = "result/distance_info" + inst_name + ".txt";
//		PrintStream ps = new PrintStream(new FileOutputStream(file), false, "gbk");
//	    System.setOut(ps);
		System.out.println();
		System.out.println("distance_info" + inst_name + "：");
	    String path1 = "data/dataB/" + inst_name + "_mianliao.csv";
		String path2 = "submit_20190923/DatasetB/DEFEATED SEEKER_" + inst_name + ".csv";
        instance inst1 = loader(path1,path2);
        cal_distance(inst1);
		System.out.println();
        
	}
	
	public static void cal_distance(instance inst) {
		double d = 0;
		double min = 1e3;
		double max_x = 0;
		double sumarea = 0;
		
		for(int i = 0; i < inst.nestpath.size(); i++) {
			NestPath p1 = inst.nestpath.get(i);
			sumarea += Math.abs(GeometryUtil.polygonArea(p1));
			for(int j = 0 ; j < p1.size(); j++) {
				Segment seg = p1.get(j);
				
                if(seg.x < min) {
                	min = seg.x;
                	if(seg.x < inst.minside)
                		System.out.println(p1.bid + " to 左边界's distance = " + seg.x);
                }
                if(inst.width - seg.x < min) {
                	min = inst.width - seg.x;
                	if(inst.width - seg.x < inst.minside)
                		System.out.println(p1.bid + " to 右边界's distance = " + (inst.width - seg.x));
                }
                if(seg.y < min) {
                	min = seg.y;
                	if(seg.y < inst.minside)
                		System.out.println(p1.bid + " to 下边界's distance = " + seg.y);
                }
                if(inst.height - seg.y < min) {
                	min = inst.height - seg.y;
                	if(inst.height - seg.y < inst.minside)
                		System.out.println(p1.bid + " to 上边界's distance = " + (inst.height - seg.y));
                }
                	
				//判断和瑕疵之间的距离
				for(int k = 0; k < inst.r.length; k++) {
					d = Math.sqrt((seg.x - inst.r_x[k])*(seg.x - inst.r_x[k]) + (seg.y - inst.r_y[k])*(seg.y - inst.r_y[k])) - inst.r[k];
					if(d < inst.linside)
					    System.out.println(p1.bid + " to 疵点" + k + "'s distance = " + d);	
					if(min > d)
						min = d;
				}
				
				if(seg.x > max_x)
					max_x = seg.x;
			}
			
			//判断零件和零件之间的距离
			for(int j = i + 1; j < inst.nestpath.size(); j++) {
				NestPath p2 = inst.nestpath.get(j);
				d = shortest_square_distance(p1, p2);
				if(d < inst.linside)
				    System.out.println(p1.bid + "to" + p2.bid + "'s distance = " + d);	
				if(min > d)
					min = d;
			}
		}
		System.out.println("min_distance = " + min);
		double u = 100 * sumarea / (max_x * inst.height);
		System.out.println("max _x = " + max_x);
		System.out.println("usage rate = " + u + "%");
	}
	
	//零件与瑕疵点之间的最小距离
    public static double nodeMinDis(NestPath A, Segment _s) {
    	double t_shortest_square_dis = Double.MAX_VALUE;
    	int prev = A.size() - 1;
		int cur = 0;
		while(prev != cur) {
			Segment seg_prev = A.get(prev);
			Segment seg_cur = A.get(cur);
			double dis = distance_segment(_s, seg_prev, seg_cur);
			if(dis < t_shortest_square_dis) {
				t_shortest_square_dis = dis;
			}
			prev = cur++;
			if(cur == A.size())
				break;
			}
		return t_shortest_square_dis;
		}
	
	public static double shortest_square_distance(NestPath A, NestPath B) {
		double t_shortest_square_dis = 1e300;
		for(int i = 0; i < A.size(); i++) {
			int prev = B.size() - 1;
			int cur = 0;
			while(prev != cur) {
				Segment seg_prev = B.get(prev);
				Segment seg_cur = B.get(cur);
				double dis = distance_segment(A.get(i), seg_prev, seg_cur);
				if(dis < t_shortest_square_dis)
					t_shortest_square_dis = dis;
				prev = cur++;
				if(cur == B.size())
					break;
				}
		}
		for(int i = 0; i < B.size(); i++) {
			int prev = A.size() - 1;
			int cur = 0;
			while(prev != cur) {
				Segment seg_prev = A.get(prev);
				Segment seg_cur = A.get(cur);
				double dis = distance_segment(B.get(i), seg_prev, seg_cur);
				if(dis < t_shortest_square_dis)
					t_shortest_square_dis = dis;
				prev = cur++;
				if(cur == A.size())
					break;
				}
		}
		return Math.sqrt(t_shortest_square_dis);
	}
	
	public static double distance_segment(Segment pt, Segment ps, Segment pe) {
	    double pqx = pe.x - ps.x;
	    double pqy = pe.y - ps.y;

		double dx = pt.x - ps.x;
		double dy = pt.y - ps.y;
		double d = pqx * pqx + pqy*pqy;
		double t = pqx*dx + pqy*dy;
		if (d > 0)
		    t /= d;
		if (t < 0)
		    t = 0;
		else if (t > 1)
		    t = 1;
		dx = ps.x + t*pqx - pt.x;
		dy = ps.y + t*pqy - pt.y;
		return dx*dx + dy*dy;
	}
	
	public static instance loader(String path1 ,String path2) throws FileNotFoundException {
		instance inst = new instance();
		//读取面料
		BufferedReader reader = null;
        String line = null;
        try {
            reader = new BufferedReader(new FileReader(path1));
        } catch (FileNotFoundException e) {
            System.out.println("[读取CSV文件，插入数据时，读取文件异常]");
            e.printStackTrace();
        }
        int lineNum = 0;
        String[] substr = null;
        String[] fieldsArr = null;
        try {
            int count = 1;
            while ((line = reader.readLine()) != null) {
                if(lineNum == 0) {
                    //表头信息
                    fieldsArr = line.split(",");
                } else {
                    //数据信息
                    String str;    
                    line += ",";               
                    Pattern pCells = Pattern
                            .compile("(\"[^\"]*(\"{2})*[^\"]*\")*[^,]*,");
                    Matcher mCells = pCells.matcher(line);
                    //读取每个单元格
                    int index = 1;
                    while (mCells.find()) {
                        str = mCells.group();
                        str = str.replaceAll(
                                "(?sm)\"?([^\"]*(\"{2})*[^\"]*)\"?.*,", "$1");
                        str = str.replaceAll("(?sm)(\"(\"))", "$2");
                        
                        if(index == 1 && count == 1) {//面料信息
                        	inst.mid = str;
                        }
                        
                        if(index == 2) {//面板信息
                        	str = str.replace("*", " ");
                        	substr = str.trim().split(" ");  	
                        	inst.width = Double.parseDouble(substr[0]);
                			inst.height = Double.parseDouble(substr[1]);
                        }
                        if(index == 3) {//疵点信息
                        	str = str.replace("[", "");
                        	str = str.replace("]", "");
                        	str = str.replace(",", " ");
                        	str = str.replace("  ", " ");
                        	substr = str.trim().split(" ");
                        	
                        	/*System.out.println(str);
                        	for(int i = 0;i<substr.length; i++) {
                        		System.out.println(substr[i]);
                        	}*/
                        	
                        	inst.r_x = new double[substr.length / 3];
                        	inst.r_y = new double[substr.length / 3];
                        	inst.r = new double[substr.length / 3];
                        	int num = 0;
                        	for(int i = 0; i < substr.length; i += 3) {
                        		inst.r_x[num] = Double.parseDouble(substr[i]);
                        		inst.r_y[num] = Double.parseDouble(substr[i + 1]);
                        		inst.r[num] = Double.parseDouble(substr[i + 2]);
                        		num ++;
                        	}
                        }
                        if(index == 4) {
                        	inst.linside = Double.parseDouble(str);
                        }
                        if(index == 5) {
                        	inst.minside = Double.parseDouble(str);
                        }
                        index++;
                        
                		inst.bin = new NestPath();
                		inst.bin.add(0,0);
                		inst.bin.add(inst.width,0);
                		inst.bin.add(inst.width,inst.height);
                		inst.bin.add(0,inst.height);
                		inst.bin.bid = 0;        
                    }
                    count++;
                }
                lineNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();    
        }
		
		//读取零件
		reader = null;
		line = null;
        try {
            reader = new BufferedReader(new FileReader(path2));
        } catch (FileNotFoundException e) {
            System.out.println("[读取CSV文件，插入数据时，读取文件异常]");
            e.printStackTrace();
        }
        lineNum = 0;
        substr = null;
        fieldsArr = null;
        try {
            int count = 1;
            while ((line = reader.readLine()) != null) {
                if (lineNum == 0) {
                    //表头信息
                    fieldsArr = line.split(",");
                } else {
                	NestPath polygon = new NestPath();
                    //数据信息
                    String str;    
                    line += ",";               
                    Pattern pCells = Pattern
                            .compile("(\"[^\"]*(\"{2})*[^\"]*\")*[^,]*,");
                    Matcher mCells = pCells.matcher(line);
                    //读取每个单元格
                    int index = 1;
                    while (mCells.find()) {
                        str = mCells.group();
                        str = str.replaceAll(
                                "(?sm)\"?([^\"]*(\"{2})*[^\"]*)\"?.*,", "$1");
                        str = str.replaceAll("(?sm)(\"(\"))", "$2");
                        
                        if(index == 4) {//坐标信息
                        	str = str.replace("[", "");
                        	str = str.replace("]", "");
                        	str = str.replace(",", "");
                        	substr = str.trim().split(" ");         	
                        	for(int i = 0; i < substr.length;i += 2) {
                				double x = Double.parseDouble(substr[i]);
                				double y = Double.parseDouble(substr[i + 1]);
                        		polygon.add(x, y);
                        	}
                        }
                        index++;
                    }
                    polygon.bid = count;
                    inst.nestpath.add(polygon);
                    count++;
                }
                lineNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();    
        }
		return inst;
	}
}
