import algorithms.DisjointSetUnion;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DisjointSetUnionTest {

    @Test
    void startsWithSeparateSets() {
        DisjointSetUnion dsu = new DisjointSetUnion(4);

        assertEquals(4, dsu.components());

        for (int vertex = 0; vertex < 4; vertex++) {
            assertEquals(vertex, dsu.find(vertex));
        }
    }

    @Test
    void mergesSetsAndRejectsRepeatedUnion() {
        DisjointSetUnion dsu = new DisjointSetUnion(4);

        assertTrue(dsu.union(0, 1));
        assertEquals(3, dsu.components());
        assertEquals(dsu.find(0), dsu.find(1));

        assertFalse(dsu.union(1, 0));
        assertFalse(dsu.union(2, 2));
        assertEquals(3, dsu.components());

        assertTrue(dsu.union(1, 2));
        assertEquals(dsu.find(0), dsu.find(2));
        assertNotEquals(dsu.find(0), dsu.find(3));
        assertEquals(2, dsu.components());
    }

    @Test
    void attachesSmallerSetToLargerSet() {
        DisjointSetUnion dsu = new DisjointSetUnion(5);

        dsu.union(0, 1);
        dsu.union(0, 2);
        dsu.union(3, 4);

        int largerRoot = dsu.find(0);
        dsu.union(3, 0);

        for (int vertex = 0; vertex < 5; vertex++) {
            assertEquals(largerRoot, dsu.find(vertex));
        }

        assertEquals(1, dsu.components());
    }

    @Test
    void countsOperations() {
        DisjointSetUnion dsu = new DisjointSetUnion(3);

        dsu.find(0);
        dsu.union(0, 1);
        dsu.union(1, 0);

        assertEquals(5L, dsu.getFindCalls());
        assertEquals(1L, dsu.getSuccessfulUnions());
        assertEquals(6L, dsu.getDsuOps());
    }

    @Test
    void makeSetResetsStateAndCounters() {
        DisjointSetUnion dsu = new DisjointSetUnion(4);
        dsu.union(0, 1);

        dsu.makeSet(3);

        assertEquals(3, dsu.components());
        assertEquals(0L, dsu.getFindCalls());
        assertEquals(0L, dsu.getSuccessfulUnions());

        for (int vertex = 0; vertex < 3; vertex++) {
            assertEquals(vertex, dsu.find(vertex));
        }
    }

    @Test
    void supportsEmptyStructureAndRejectsInvalidInput() {
        DisjointSetUnion empty = new DisjointSetUnion(0);
        assertEquals(0, empty.components());

        assertThrows(
                IllegalArgumentException.class,
                () -> new DisjointSetUnion(-1)
        );

        DisjointSetUnion dsu = new DisjointSetUnion(3);

        assertThrows(
                IllegalArgumentException.class,
                () -> dsu.find(-1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> dsu.find(3)
        );
    }

    @Test
    void matchesReferenceAfterRandomUnions() {
        Random random = new Random(42);

        for (int trial = 0; trial < 50; trial++) {
            int n = 1 + random.nextInt(40);
            DisjointSetUnion dsu = new DisjointSetUnion(n);
            int[] group = new int[n];
            int referenceComponents = n;

            for (int vertex = 0; vertex < n; vertex++) {
                group[vertex] = vertex;
            }

            for (int operation = 0; operation < 100; operation++) {
                int first = random.nextInt(n);
                int second = random.nextInt(n);

                int firstGroup = group[first];
                int secondGroup = group[second];
                boolean shouldMerge = firstGroup != secondGroup;

                if (shouldMerge) {
                    for (int vertex = 0; vertex < n; vertex++) {
                        if (group[vertex] == secondGroup) {
                            group[vertex] = firstGroup;
                        }
                    }

                    referenceComponents--;
                }

                assertEquals(shouldMerge, dsu.union(first, second));
                assertEquals(referenceComponents, dsu.components());

                for (int a = 0; a < n; a++) {
                    for (int b = 0; b < n; b++) {
                        boolean referenceConnected = group[a] == group[b];
                        boolean dsuConnected = dsu.find(a) == dsu.find(b);

                        assertEquals(referenceConnected, dsuConnected);
                    }
                }
            }
        }
    }
}