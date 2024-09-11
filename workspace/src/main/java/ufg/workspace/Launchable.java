package ufg.workspace;

public interface Launchable {
    public default boolean validate(String[] args) { return true; }
    public void launch(String[] args);
}
