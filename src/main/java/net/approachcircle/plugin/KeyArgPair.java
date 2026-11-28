package net.approachcircle.plugin;


public class KeyArgPair {
    public String key;
    public Object[] args;

    public KeyArgPair(String key, Object... args) {
        this.key = key;
        this.args = args;
    }
}
