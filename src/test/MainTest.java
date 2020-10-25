package test;

public class MainTest {
    public static void main(String[] args) {
        String s = "hello there hey";
        System.out.println(s.length());

        StringBuilder sb = new StringBuilder();

        int[] arr = {65, 75, 99, 101, 13, 10};
        for (int i : arr) {
            sb.append((char)i);
        }

        System.out.println(sb.toString() + " " + sb.toString().length());
    }
}
