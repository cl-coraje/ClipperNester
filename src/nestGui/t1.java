package nestGui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class t1 {

    /*
    快速的生成svg文档
     */
    public static void main(String[] args) throws UnsupportedEncodingException, SVGGraphics2DIOException {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgURI = "http://www.w3.org/2000/svg";
        Document doc = domImpl.createDocument(svgURI, "svg", null);     //创建SVG文件
        SVGGraphics2D svggener = new SVGGraphics2D(doc);                                   //将文档包装进对象
        t1 test = new t1();
        test.paint(svggener);                                                              //使用对象画图的时候，自动将图文件保存成svg文档
        //输出SVG文件
        Writer out = new OutputStreamWriter(System.out, "UTF-8");
        svggener.stream(out, true);
    }
    //画图
    public void paint(Graphics2D g2d) {
        g2d.setPaint(Color.red);
        //画一个矩形
        g2d.fill(new Rectangle(10, 10, 100, 100));
    }
}
