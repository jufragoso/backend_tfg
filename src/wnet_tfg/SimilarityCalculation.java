package wnet_tfg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class SimilarityCalculation {
	
	public static int contador = 0;
	
	private static final String FILENAME1 = "input/themes.txt";
	private static final String FILENAME2 = "input/categories.txt";

	private static ILexicalDatabase db = new NictWordNet();

	private static double compute(String word1, String word2) {
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
		return s;
	}

	public static void escribirResultado(String nameFile, String word, double resultado){
		FileWriter fichero = null;
		PrintWriter pw = null;
		
		try {
			
			fichero = new FileWriter("output/"+nameFile+".txt",true);
			pw = new PrintWriter(fichero);
			
			pw.println(word + " - " + resultado);
			
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			// aprovechamos el finally para cerrar el fichero
			
			try {
				if(fichero!= null){
					fichero.close();
				}
			} catch (IOException e2) {
				
				e2.printStackTrace();
			}

			
		}
	}

	public static void comparar() {

		
		BufferedReader br1, br2 = null;
		FileReader fr1, fr2 = null;

		try {



			fr2 = new FileReader(FILENAME2);
			br2 = new BufferedReader(fr2);

			String currentLine1, currentLine2;

			while ((currentLine2 = br2.readLine()) != null) {
				fr1 = new FileReader(FILENAME1);
				br1 = new BufferedReader(fr1);
				while ((currentLine1 = br1.readLine()) != null) {
					double distancia = compute(currentLine2, currentLine1);
					if(distancia >=0.4){
					contador++;
					escribirResultado(currentLine2, currentLine1, distancia);
					}
				}
			}

		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {

		}

	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		comparar();
		System.out.println("Ejecutado, elementos --> "+ contador);
	}

}
