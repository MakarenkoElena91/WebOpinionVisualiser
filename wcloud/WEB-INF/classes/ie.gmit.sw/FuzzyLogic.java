package ie.gmit.sw;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

public class FuzzyLogic {
    public static int getScore(int title, int heading, int body) {
        FIS fis = FIS.load("fuzzy.fcl", true);
        FunctionBlock functionBlock = fis.getFunctionBlock("score");

        fis.setVariable("title", title);
        fis.setVariable("heading", heading);
        fis.setVariable("body", body);

        fis.evaluate();
        return (int) fis.getVariable("score").defuzzify();
    }
//    public static void main(String[] args) {
//        System.out.println("Fuzzy score is: " + new FuzzyLogic().getScore(100,60, 68));
//    }
}