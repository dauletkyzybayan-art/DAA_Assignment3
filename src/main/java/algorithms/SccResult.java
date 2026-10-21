package algorithms;

public class SccResult {
    private final int[] component;
    private final int componentCount;

    public SccResult(int[] component, int componentCount) {
        this.component = component.clone();
        this.componentCount = componentCount;
    }

    public int getComponent(int vertex) {
        return component[vertex];
    }

    public int getComponentCount() {
        return componentCount;
    }
}