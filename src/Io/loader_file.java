package Io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Data.NestPath;
import Io.instance;
import Util.GeometryUtil;

public class loader_file {

	public static instance loader(String path1, String path2) throws FileNotFoundException {
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
        double minarea = 1e10;
        double allarea = 0;
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
                        
                        if(index == 1 && count == 1) {                //批次信息
                        	inst.pid = str;
                        }
                        if(index == 4) {                              //坐标信息
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
                        if(index == 5) {                                //旋转角度信息
                        	str = str.replace(",", "");
                        	substr = str.trim().split(" ");
                        	for(int i = 0; i < substr.length;i++) {
                        		polygon.rotations[i] = Integer.parseInt(substr[i]);
                        	}
                        }
                        if(index == 6 && count == 1) {
                        	inst.mid = str;
                        }
                        index++;
                    }
                    polygon.bid = count;
                    polygon.length = Math.abs(GeometryUtil.polygonLength(polygon));
                    polygon.area = Math.abs(GeometryUtil.polygonArea(polygon));
                    if(polygon.area < minarea)
                    	minarea = polygon.area;
                    allarea += polygon.area;
                    polygon.Zerolize();
                    inst.nestpath.add(polygon);
                    count++;
                }
                lineNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();    
        }
        inst.sumarea = allarea;
        inst.minarea = minarea;
             
      //将疵点当做零件加入nestpath  外切十二边形
      //疵点
      		int count1 = -1;
      		for(int i = 0; i < inst.r.length; i++) {
      			double lr = inst.r[i] / (Math.cos(15 * Math.PI / 180));
      			double sin_lr = lr * Math.sin(Math.PI / 6);
      			double cos_lr = lr * Math.cos(Math.PI / 6);
      			
      			NestPath nest1 = new NestPath();
      			nest1.add(inst.r_x[i], inst.r_y[i] + lr);
      			nest1.add(inst.r_x[i] + sin_lr, inst.r_y[i] + cos_lr);
      			nest1.add(inst.r_x[i] + cos_lr, inst.r_y[i] + sin_lr);
      			nest1.add(inst.r_x[i] + lr, inst.r_y[i]);
      			nest1.add(inst.r_x[i] + cos_lr, inst.r_y[i] - sin_lr);
      			nest1.add(inst.r_x[i] + sin_lr, inst.r_y[i] - cos_lr);
      			nest1.add(inst.r_x[i], inst.r_y[i] - lr);
      			nest1.add(inst.r_x[i] - sin_lr, inst.r_y[i] - cos_lr);
      			nest1.add(inst.r_x[i] - cos_lr, inst.r_y[i] - sin_lr);
      			nest1.add(inst.r_x[i] - lr, inst.r_y[i]);
      			nest1.add(inst.r_x[i] - cos_lr, inst.r_y[i] + sin_lr);
      			nest1.add(inst.r_x[i] - sin_lr, inst.r_y[i] + cos_lr);
      			nest1.add(inst.r_x[i], inst.r_y[i] + lr);
      			nest1.bid = count1;
      			nest1.setRotation(0);
      			inst.nestpath.add(nest1);
      			count1--;
      		}
        
        		
		return inst;
	}
}
