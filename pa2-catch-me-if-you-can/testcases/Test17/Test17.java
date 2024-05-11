import java.util.*;

public class Test17 {

    static class ArrayObject {
        int value;

        ArrayObject(int val) {
            this.value = val;
        }
    }

    public static void main(String[] args) {
        ArrayObject[] array = new ArrayObject[5];
        for(int i=0;i<5;i++)
        {
            array[i] = new ArrayObject(i);
            if(i%3==0)
            {
                testMethod(array[i]);
            }
            System.out.println(array[i].value);
        } 
        
    }

    static void testMethod(ArrayObject ar) 
    { 
        System.out.println("Testing: " + ar.value);
    }
}
