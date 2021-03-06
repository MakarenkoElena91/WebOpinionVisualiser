package ie.gmit.sw;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

import java.io.File;
import java.io.IOException;

public class FuzzyLogic {
    /**
     * get the score based on fuzzy.fcl rules
     * @param title - text that appears in tag title
     * @param heading - text that appears in heading tags: h1 & h2
     * @param body - text that appears in tag body
     * @return fuzzy score
     */
    public static int getScore(int title, int heading, int body) {
        FIS fis = FIS.load("../webapps/wcloud/res/fuzzy.fcl", true);
        FunctionBlock functionBlock = fis.getFunctionBlock("score");

        fis.setVariable("title", title);
        fis.setVariable("heading", heading);
        fis.setVariable("body", body);

        fis.evaluate();
        return (int) fis.getVariable("score").defuzzify();
    }
    //for testing
    public static void main(String[] args) throws IOException {
        System.out.println("Fuzzy score is: " + new FuzzyLogic().getScore(100,60, 500));
        String basePath = new File("res/fuzzy.fcl").getCanonicalPath();
        System.out.println(basePath);

    }
}