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
    ���ٵ�����svg�ĵ�
     */
    public static void main(String[] args) throws UnsupportedEncodingException, SVGGraphics2DIOException {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgURI = "http://www.w3.org/2000/svg";
        Document doc = domImpl.createDocument(svgURI, "svg", null);     //����SVG�ļ�
        SVGGraphics2D svggener = new SVGGraphics2D(doc);                                   //���ĵ���װ������
        t1 test = new t1();
        test.paint(svggener);                                                              //ʹ�ö���ͼ��ʱ���Զ���ͼ�ļ������svg�ĵ�
        //���SVG�ļ�
        Writer out = new OutputStreamWriter(System.out, "UTF-8");
        svggener.stream(out, true);
    }
    //��ͼ
    public void paint(Graphics2D g2d) {
        g2d.setPaint(Color.red);
        //��һ������
        g2d.fill(new Rectangle(10, 10, 100, 100));
    }
}
