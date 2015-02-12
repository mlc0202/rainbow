package com.icitic.ant.ext;

public class Namespace {

    private String ns;

    private String dir;

    private boolean ignore = false;

    public String getNs() {
        return ns;
    }

    public void setNs(String ns) {
        this.ns = ns + '.';
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

}
