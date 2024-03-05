/* Dominator of statement */

public class Testcase5 {

    public static void main(String[] args) {
        String x = new String();
        x = "Animal";

        if(isCat()){
            x = "Cat";
        }else{
            x = "Dog";
        }

        System.out.println(x);
    }

    public static boolean isCat(){
        return true;
    }
}
