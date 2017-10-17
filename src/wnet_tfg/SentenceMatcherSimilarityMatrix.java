package wnet_tfg;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;

public class SentenceMatcherSimilarityMatrix {
	private static ILexicalDatabase db = new NictWordNet();

	public double[][] getSimilarityMatrix(String[] words1, String[] words2, RelatednessCalculator rc) {
		double[][] result = new double[words1.length][words2.length];
		for (int i = 0; i < words1.length; i++) {
			for (int j = 0; j < words2.length; j++) {
				double score = rc.calcRelatednessOfWords(words1[i], words2[j]);
				result[i][j] = score;
			}
		}
		return result;
	}

	private void compute(String[] words1, String[] words2) {
		System.out.println("WuPalmer");
		RelatednessCalculator rc1 = new WuPalmer(db);
		{
			double[][] s1 = getSimilarityMatrix(words1, words2, rc1);
			for (int i = 0; i < words1.length; i++) {
				for (int j = 0; j < words2.length; j++) {
					System.out.print(s1[i][j] + "\t");
				}
				System.out.println();
			}
		}
		
	}

	public static void main(String[] args) {
		String sent1 = "Administration & Finances";
		String sent2 = "Administration & Finances";

		String[] words1 = sent1.split(" ");
		String[] words2 = sent2.split(" ");
		SentenceMatcherSimilarityMatrix sm1 = new SentenceMatcherSimilarityMatrix();
		sm1.compute(words1, words2);
	}
}
