import algorithms.DisjointSetUnion;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DisjointSetUnionRandomTest {

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