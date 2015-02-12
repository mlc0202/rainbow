package com.icitic.core.db.jdbc;

public abstract class AtomEx<T> extends Atom {

    protected T result;

    public T getResult() {
        return result;
    }
}
