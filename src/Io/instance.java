package Io;

import java.util.ArrayList;

import Data.NestPath;

public class instance {
	 public double height;                                                  //面料长度 
	 public double width;                                                   //面料宽度
	 public double linside;                                                 //零件之间的距离
	 public double minside;                                                 //面料之间的距离
	 public String pid;                                                     //下料批次编号
     public String mid;                                                     //面料编号
     public NestPath bin;                                                   //面料
     public double minarea;                                                 //最小的零件面积
     public double sumarea;                                                 //零件总面积
     public ArrayList<NestPath> nestpath = new ArrayList<NestPath>();       //零件序列
	 //瑕疵信息
	 public double[] r_x;
	 public double[] r_y;
	 public double[] r;    
	 
	 public int sort_selection;//1 for true area, 2 for rec area初始零件排序规则
	 
	 public int iter_number;//随机交换获得新解的次数
	 
	 
	 
}
