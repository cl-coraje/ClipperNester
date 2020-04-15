package Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import Algorithm.Nest;
import Data.NestPath;
import Data.Placement;
import Io.distance_test;
import Io.instance;
import Io.loader_file;
import Io.polygon_test;
import Io.testhtml;
import Io.write_file;
import Util.Config;

public class Nest_test {
	
	public static void main(String[] args) throws Exception {
		String inst_name1 = "L0004";
	    int iter_num = 10;                //随机交换迭代次数
	    int sort_selection = 2;           //1 for true area, 2 for rec area初始零件排序规则
	    solve(inst_name1,5,sort_selection,iter_num);
	}

	public static void solve(String inst_name, double plus, int sort_selection, int iter_num) throws Exception {
		DecimalFormat df = new DecimalFormat("#.00");
		double t1 = System.nanoTime() / 1e9;
		System.out.println("开始剪裁：" + inst_name + "\t" + "开始时间：" + t1);
		String path1 = "data/dataB/" + inst_name + "_mianliao.csv";
		String path2 = "data/dataB/" + inst_name + "_lingjian.csv";
		instance inst = loader_file.loader(path1,path2);
		inst.sort_selection = sort_selection;
		inst.iter_number = iter_num;

		polygon_test.run(inst.nestpath);

		ArrayList<NestPath> part1 = new ArrayList<NestPath>();
		for(int i = 0; i < inst.nestpath.size(); i++) {
			part1.add(new NestPath(inst.nestpath.get(i)));
		}
		Config config = new Config();
		config.SPACING = inst.linside + plus;
		System.out.println("CURVE_DISTANCE = " + config.CURVE_DISTANCE);
		System.out.println("零件间间隙 = " + config.SPACING);
		Nest nest = new Nest(inst.bin, part1, config, inst);
		List<Placement> appliedPlacement = nest.startNest();

		//去除疵点
		for(int i = 0; i < inst.r.length; i++) {
			part1.remove(part1.size() - 1);
		}
		//输出结果
		write_file.save_result(appliedPlacement, part1 , inst);

		//输出利用率等信息；
		String un = df.format(nest.Utilization(appliedPlacement) * 100);
		System.out.println(inst_name + "的最优利用率是:" + un +"%");
		double t2 = System.nanoTime() / 1e9;
		System.out.println("结束剪裁：" + inst_name + "\t" + "结束时间：" + t2);
		System.out.println(inst.pid + "批次剪切完成 "+"\t"+"耗时: " + df.format(t2 - t1));
		//距离检测
		distance_test.run(inst_name);
		testhtml.run(inst_name);
	}
}
