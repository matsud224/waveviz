package com.github.matsud224.waveviz;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

public class HierarchyTree implements TreeModel {
    private final String scopeName;
    private final String scopeType;
    private final HierarchyTree parent;
    public final ArrayList<HierarchyTree> children;
    public final ArrayList<Signal> signals;

    public HierarchyTree(String scopeName, String scopeType, HierarchyTree parent) {
        this.scopeName = scopeName;
        this.scopeType = scopeType;
        this.parent = parent;
        children = new ArrayList<>();
        signals = new ArrayList<>();
    }

    public String getScopeName() {
        return scopeName;
    }

    public String getScopeType() {
        return scopeType;
    }

    public HierarchyTree getParent() {
        return parent;
    }

    public void print(int depth) {
        for (var i = 0; i < depth; i++)
            System.out.print("-");
        System.out.printf("%s (%s)\n", scopeName, scopeType);
        for (var h : children) {
            h.print(depth + 1);
        }
        for (var s : signals) {
            s.print(depth + 1);
        }
    }

    @Override
    public String toString() {
        return scopeName;
    }

    @Override
    public Object getRoot() {
        return parent == null ? this : parent.getRoot();
    }

    @Override
    public Object getChild(Object o, int i) {
        return ((HierarchyTree) o).children.get(i);
    }

    @Override
    public int getChildCount(Object o) {
        return ((HierarchyTree) o).children.size();
    }

    @Override
    public boolean isLeaf(Object o) {
        return false;
    }

    @Override
    public void valueForPathChanged(TreePath treePath, Object o) {

    }

    @Override
    public int getIndexOfChild(Object o, Object o1) {
        return ((HierarchyTree) o).children.indexOf(o1);
    }

    @Override
    public void addTreeModelListener(TreeModelListener treeModelListener) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener treeModelListener) {

    }
}
