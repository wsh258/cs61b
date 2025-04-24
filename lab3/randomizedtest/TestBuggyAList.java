package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        BuggyAList<Integer> lld1 = new BuggyAList<>();
        AListNoResizing<Integer> lld2 = new AListNoResizing<>();

        lld1.addLast(4);
        lld1.addLast(5);
        lld1.addLast(6);

        lld2.addLast(4);
        lld2.addLast(5);
        lld2.addLast(6);

        assertEquals(lld1.removeLast(), lld2.removeLast());
        assertEquals(lld1.removeLast(), lld2.removeLast());
        assertEquals(lld1.removeLast(), lld2.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                broken.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int sizeB = broken.size();



            }
            else if (operationNumber == 2) {
                if (L.size() != 0) {
                    int last = L.getLast();
                    int lastB = broken.getLast();
                }
            }
            else if (operationNumber == 3) {
                if (L.size() != 0) {
                    int last = L.removeLast();
                    int lastB = broken.removeLast();

                }
            }
        }
    }
}
