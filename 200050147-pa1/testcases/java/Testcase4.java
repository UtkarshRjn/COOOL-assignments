/* Null Pointer Checker */

public class Testcase4 {

    public static void main(String[] args) {
        // Case 1: Basic Null Check
        String str1 = new String();
        str1 = "NonNullString";

        System.out.println(str1);

        // Case 2: Conditional Operator
        String str2 = Math.random() < 0.5 ? "NonNullString" : null;
        System.out.println(str2);

    }
}
