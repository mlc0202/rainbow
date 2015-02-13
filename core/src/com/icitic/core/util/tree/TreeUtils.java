package com.icitic.core.util.tree;

import java.util.ArrayList;
import java.util.List;

import com.icitic.core.model.object.ITreeObject;
import com.icitic.core.model.object.TreeNode;

public abstract class TreeUtils {

    /**
     * 返回一个节点下所有的id
     * 
     * @param treeNode
     * @return
     */
    public static final <I, T extends ITreeObject<I>> List<I> getIds(TreeNode<T> treeNode) {
        final List<I> result = new ArrayList<I>();
        procTreeNode(treeNode, new TreeNodeProcessor<T>() {
            @Override
            public boolean process(TreeNode<T> node) {
                result.add(node.getObj().getId());
                return true;
            }
        });
        return result;
    }

    /**
     * 对一个树节点进行处理
     * 
     * @param <T>
     * @param node
     * @param processor
     */
    public static <T> void procTreeNode(TreeNode<T> node, TreeNodeProcessor<T> processor) {
        if (!processor.process(node))
            return;
        if (processor.isStop())
            return;
        if (!node.isLeaf()) {
            for (TreeNode<T> child : node.getChildren()) {
                procTreeNode(child, processor);
                if (processor.isStop())
                    return;
            }
            processor.afterProcChildren(node);
        }
    }

    /**
     * 对森林进行处理
     * 
     * @param <T>
     * @param tree
     * @param processor
     */
    public static <T> void procForest(List<TreeNode<T>> trees, TreeNodeProcessor<T> processor) {
        for (TreeNode<T> tree : trees) {
            procTreeNode(tree, processor);
            if (processor.isStop())
                break;
        }
    }

}
