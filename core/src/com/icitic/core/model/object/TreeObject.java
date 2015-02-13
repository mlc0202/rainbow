package com.icitic.core.model.object;

/**
 * 树形对象基类
 * 
 * @author lijinghui
 * 
 */
public class TreeObject<I> extends IdObject<I> implements ITreeObject<I> {

    private static final long serialVersionUID = 1L;

    private I pid;

    @Override
    public I getPid() {
        return pid;
    }

    public void setPid(I pid) {
        this.pid = pid;
    }

}
