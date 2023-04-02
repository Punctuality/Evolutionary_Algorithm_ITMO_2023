package additional1.util;

import java.util.Random;

public class ArrayUtil {
    public static void swapElement(int[] a1, int[] a2, int idx) {
        int tmp = a1[idx];
        a1[idx] = a2[idx];
        a2[idx] = tmp;
    }

    public static void swapElement(int[] a, int idx1, int idx2) {
        int tmp = a[idx1];
        a[idx1] = a[idx2];
        a[idx2] = tmp;
    }

    public static void scrambleArray(int[] arr, Random random) {
        for (int i = arr.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            swapElement(arr, index, i);
        }
    }

    public static void reverse(int[] arr) {
        for (int i = 0; i < arr.length / 2; i++) swapElement(arr, i, arr.length - i - 1);
    }
}
