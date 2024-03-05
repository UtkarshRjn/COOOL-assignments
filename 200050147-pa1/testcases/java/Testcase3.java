package testcases.java;

/* Live variable analysis */
public class Testcase3 {

    public static void main(String[] args) {
        
        Integer x = 5;
        Integer y = x + 3;
        Integer z = y * 2;
        Integer result = z;
        System.out.println(result);    
    }

}
