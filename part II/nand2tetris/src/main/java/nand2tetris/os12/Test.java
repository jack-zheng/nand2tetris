package nand2tetris.os12;

public class Test {
    public static void main(String[] args) {
        System.out.println(divide(100, 7));
    }

    public static int divide(int x, int y) {
        if (y > x) return 0;
        int q = divide(x, 2 * y);
        if ((x - 2 * q * y) < y) {
            return 2 * q;
        } else {
            return 2 * q + 1;
        }
    }
}
