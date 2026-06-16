package com.intechcore.scomponents.tools.cutter.processor.helpers;

import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

public class ReplaceHelper {
    private final TreeMaker treeMaker;
    private final JavacElements elementUtils;

    public ReplaceHelper(TreeMaker treeMaker, JavacElements elementUtils) {
        this.treeMaker = treeMaker;
        this.elementUtils = elementUtils;
    }

    public JCTree.JCExpression createClassExpression(String className) {
        String[] split = className.split("\\.");

        JCTree.JCExpression select = this.treeMaker.Ident(this.elementUtils.getName(split[0]));

        for (int i = 1; i < split.length; i++) {
            select = this.treeMaker.Select(select, this.elementUtils.getName(split[i]));
        }

        return select;
    }
}

