package Clipper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Clipper.Clipper.EndType;
import Clipper.Clipper.JoinType;
import Clipper.Point.LongPoint;

public class PolyNode {

    enum NodeType {
        ANY, OPEN, CLOSED
    }

    private PolyNode parent;
    private final Path polygon = new Path();
    private int index;
    private JoinType joinType;
    private EndType endType;
    final List<PolyNode> childs = new ArrayList<>();
    private boolean isOpen;

    public void addChild( PolyNode child ) {
        final int cnt = childs.size();
        childs.add( child );
        child.parent = this;
        child.index = cnt;
    }

    public int getChildCount() {
        return childs.size();
    }

    public List<PolyNode> getChilds() {
        return Collections.unmodifiableList( childs); //返回一个只读的List;
    }

    public List<LongPoint> getContour() {
        return polygon;                               //记录的消除重点之后的多边形
    }

    public EndType getEndType() {
        return endType;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public PolyNode getNext() {
        if (!childs.isEmpty()) {
            return childs.get( 0 );
        }
        else {
            return getNextSiblingUp();
        }
    }

    private PolyNode getNextSiblingUp() {
        if (parent == null) {
            return null;
        }
        else if (index == parent.childs.size() - 1) {
            return parent.getNextSiblingUp();
        }
        else {
            return parent.childs.get( index + 1 );
        }
    }

    public PolyNode getParent() {
        return parent;
    }

    public Path getPolygon() {
        return polygon;
    }

    public boolean isHole() {
        return isHoleNode();
    }

    private boolean isHoleNode() {
        boolean result = true;
        PolyNode node = parent;
        while (node != null) {
            result = !result;
            node = node.parent;
        }
        return result;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setEndType( EndType value ) {
        endType = value;
    }

    public void setJoinType( JoinType value ) {
        joinType = value;
    }

    public void setOpen( boolean isOpen ) {
        this.isOpen = isOpen;
    }

    public void setParent( PolyNode n ) {
        parent = n;

    }

}
