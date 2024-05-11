import soot.*;

public class PA1{
    public static void main(String[] args){
        String classPath = "sootclasses-trunk-jar-with-dependencies.jar:./testcases/java/";

        String[] sootArgs = {
            "-cp", classPath,
            "-pp", "-f", "jimple", "-p",
            args[0], "on", args[1], "-print-tags", "-keep-line-number",
            "-d", "./testcases/jimple/"
        };

        soot.Main.main(sootArgs);
    }
}