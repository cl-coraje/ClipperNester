package Util;

public class Config {
	public  static int CLIIPER_SCALE = 100;          //小数后保留的位数
    public static  double CURVE_TOLERANCE;   //当误差在多小的时候可以近似为矩形
    public  double CURVE_DISTANCE;
    public  double SPACING;                         //零件两两之间的距离
    public  int POPULATION_SIZE;                     //种群个体数量
    public  int MUTATION_RATE;                      //变异概率

    public Config() {
        CLIIPER_SCALE = 100;
        CURVE_TOLERANCE = 0.25;
        CURVE_DISTANCE = 0.8;
        SPACING = 5;
        POPULATION_SIZE = 10;
        MUTATION_RATE = 10;
    }
}
