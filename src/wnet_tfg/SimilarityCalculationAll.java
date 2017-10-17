package wnet_tfg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.PorterStemmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class SimilarityCalculationAll {

	public static int contador = 0;

	public static double distanciaF = 0.0;

	public static int[] solF;

	private static final String FILENAME1 = "input2/themes_all.txt";
	private static final String FILENAME2 = "input2/categories.txt";

	private static ILexicalDatabase db = new NictWordNet();

	public static double compute(String word1, String word2) {
		WS4JConfiguration.getInstance().setMFS(true);
		RelatednessCalculator rc = new WuPalmer(db);
		PorterStemmer stemmer = new PorterStemmer();

		if (word1.endsWith("s") || word1.endsWith("es")) {
			word1 = stemmer.stemWord(word1);
		}
		if (word1.endsWith("s") || word1.endsWith("es")) {
			word2 = stemmer.stemWord(word2);
		}

		// double s = new WuPalmer(db).calcRelatednessOfWords("administration",
		// "audits");
		// return s;

		List<POS[]> posPairs = rc.getPOSPairs();
		double maxScore = -1D;

		for (POS[] posPair : posPairs) {
			List<Concept> synsets1 = (List<Concept>) db.getAllConcepts(word1, posPair[0].toString());
			List<Concept> synsets2 = (List<Concept>) db.getAllConcepts(word2, posPair[1].toString());

			for (Concept synset1 : synsets1) {
				for (Concept synset2 : synsets2) {
					Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
					double score = relatedness.getScore();
					if (score > maxScore) {
						maxScore = score;
					}
				}
			}
		}

		if (maxScore == -1D) {
			maxScore = 0.0;
		}

		return maxScore;

		// double s = new WuPalmer(db).calcRelatednessOfWords("administration",
		// "audits");
		// return s;
	}

	private static void escribirResultado(String nameFile, String word, double resultado) {
		FileWriter fichero = null;
		PrintWriter pw = null;

		try {

			fichero = new FileWriter("output2/" + nameFile + ".txt", true);
			pw = new PrintWriter(fichero);

			pw.println(word + " - " + resultado);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// aprovechamos el finally para cerrar el fichero

			try {
				if (fichero != null) {
					fichero.close();
				}
			} catch (IOException e2) {

				e2.printStackTrace();
			}

		}
	}

	private static double comparar(String word1, String word2) {
		double distancia = 0;
		distancia = compute(word1, word2);
		return distancia;
	}

	private static double compararSimpleCompuesto(String word, String[] array) {
		double[] arrayDistancia = new double[array.length];
		double distancia = 0;

		for (int i = 0; i < array.length; i++) {
			arrayDistancia[i] = compute(word, array[i]);
		}

		for (int i = 0; i < array.length; i++) {
			distancia += arrayDistancia[i];
		}

		distancia = distancia / arrayDistancia.length;

		return distancia;
	}

	private static int comprobarMejorResultado(double resultadoParcial[]) {
		double mayor = resultadoParcial[0];
		int indice = 0;

		for (int i = 0; i < resultadoParcial.length; i++) {
			if (resultadoParcial[i] > mayor) {
				indice = i;
			}
		}

		return indice;

	}

	// public static double[] comparar(String word, String []categories){
	// double [] resultados = new double[categories.length];
	// double distancia;
	// for(int i =0; i < categories.length; i++){
	// distancia = 0;
	// distancia = compute(word, categories[i]);
	// resultados[i] = distancia;
	// }
	// return resultados;
	// }

	// private static int seleccionar(double []resultados){
	// int indice = 0;
	// double mayor = 0;
	// for(int i =0; i< resultados.length;i++){
	// if(mayor < resultados[i]){
	// indice = i;
	// mayor = resultados[i];
	// }
	// }
	// if(mayor >= 0.4)
	// return indice;
	// else
	// return -1;
	// }

	public static void procesar() {

		BufferedReader br1, br2 = null;
		FileReader fr1 = null;
		FileReader fr2 = null;
		String tema, titulo, palabraProcesada;
		ArrayList<String> categorias = new ArrayList<String>();
		ArrayList<String> temas = new ArrayList<String>();
		double[] resultadoParcial;
		double distancia;
		int errores = 0;
		// int contador = 0;
		// int calculo = 0;
		// int porcentaje = 0;
		// int totalTemas = 0;

		try {

			String currentLine1, currentLine2;

			// abro fichero themes_all.txt
			fr1 = new FileReader(FILENAME1);

			// abro fichero categories.txt
			br1 = new BufferedReader(fr1);

			fr2 = new FileReader(FILENAME2);
			br2 = new BufferedReader(fr2);

			while ((currentLine2 = br2.readLine()) != null) {
				categorias.add(currentLine2);
			}

			while ((currentLine1 = br1.readLine()) != null) {
				temas.add(currentLine1);
			}
			// leo temas
			// nt i = temas.size();

			for (int i = 0; i < temas.size(); i++) {
				currentLine1 = temas.get(i);
				// String [] parts = currentLine1.split(";");
				//
				// tema= parts[0];
				// titulo = parts[1];
				// if(tema.isEmpty()) {
				// palabraProcesada= titulo;
				// }else {
				// palabraProcesada = tema;
				// }

				palabraProcesada = currentLine1;

				resultadoParcial = new double[categorias.size()];
				int contador = 0;
				int categoriaActual = 0;

				for (int j = 0; j < categorias.size(); j++) {
					currentLine2 = categorias.get(j);
					// System.out.println("Palabra " + contador);
					if (!currentLine2.equals("")) {
						// si categoria y tema son palabras compuestas
						if (palabraProcesada.contains(" ") && currentLine2.contains(" ")) {

							String[] array1 = currentLine1.split(" ");
							String[] array2 = currentLine2.split(" ");
							distancia = compararCompuesto(array1, array2);


							// si el tema es compuesto y la categoria es simple
						} else if (currentLine1.contains(" ") && !currentLine2.contains(" ")) {
							String[] array1 = currentLine1.split(" ");
							distancia = compararSimpleCompuesto(currentLine2, array1);
	
							// si el tema es simple y la categoria compuesta
						} else if (!palabraProcesada.contains(" ") && currentLine2.contains(" ")) {
							String[] array2 = dividirPalabras(currentLine2);
							distancia = compararSimpleCompuesto(currentLine1, array2);


							// si tema y categoria son simples
						} else {
							distancia = comparar(currentLine1, currentLine2);

						}
						
						resultadoParcial[contador] = distancia;
					}
					
					
					categoriaActual++;
					contador++;
				}

				int mejorResultado = comprobarMejorResultado(resultadoParcial);

				if (resultadoParcial[mejorResultado] >= 0.2) {
					 escribirResultado(categorias.get(mejorResultado), palabraProcesada, resultadoParcial[mejorResultado]);
				}else {
					escribirResultado("Otros", palabraProcesada, resultadoParcial[mejorResultado]);
					errores++;
				}

				i++;
				System.out.println("Palabra " + i);

			}

		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			try {
				if (null != fr1) {
					fr1.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("Errores : " + errores);

	}

	private static String[] dividirPalabras(String currentLine) {
		String[] resultado;
		if (currentLine.contains(" ")) {
			resultado = currentLine.split(" ");
		} else {
			resultado = new String[1];
			resultado[0] = currentLine;
		}
		return resultado;
	}

	private static boolean yaExiste(int array[], int valor) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == valor) {
				return true;
			}
		}
		return false;
	}

	private static boolean backtracking(int etapa, int longitud, int esSolucion, double matrizResultados[][],
			int arrayIndices[], double arrayResultados[]) {
		double mayor;
		for (int i = 0; i < longitud; i++) {
			mayor = matrizResultados[etapa][i];
			if (!yaExiste(arrayIndices, i)) {
				arrayIndices[etapa] = i;
				arrayResultados[etapa] = mayor;
				if (arrayResultados[esSolucion - 1] != 1) {
					return true;
				} else {
					backtracking(etapa + 1, longitud, esSolucion, matrizResultados, arrayIndices, arrayResultados);
				}
			}
		}

		return true;
	}

	public static boolean opcionVailda(int[] solP, double[][] matriz, int valor) {

		if (ArrayUtils.contains(solP, valor)) {
			return false;
		}

		return true;
	}

	public static void actualizarSolucion(int[] solP) {
		for (int i = 0; i < solP.length; i++) {
			solF[i] = solP[i];
		}
		
	}

	public static boolean esSolucionFinal(int[] solP, double[][] matriz) {

		double distanciaP = 0.0;

		if (ArrayUtils.contains(solP, -1)) {
			return false;
		}
		for (int i = 0; i < matriz.length; i++) {
			distanciaP += matriz[i][solP[i]];
		}

		if (distanciaP <= distanciaF) {
			return false;
		}

		return true;
	}

	public static void backtracking2(int etapa, double[][] matriz, int[] solP) {
		if (etapa < matriz.length) {
			for (int i = 0; i < matriz.length; i++) {

				if (opcionVailda(solP, matriz, i)) {
					solP[etapa] = i;

					if (esSolucionFinal(solP, matriz)) {
						actualizarSolucion(solP);
					} else {
						backtracking2(etapa + 1, matriz, solP);
					}
				}else {
					solP[etapa] = -1;
				}
			}
		}
	}
	
	public static double resultadoMatriz(double[][]matriz) {
		double distancia =0.0;
		int contador =0;
		for(int i=0; i<matriz.length; i++) {
			for(int j=0; j<matriz[i].length; j++) {
				distancia+=matriz[i][j];
				contador++;
			}
		}
		
		return distancia/contador;
	}

	private static double compararCompuesto(String[] array1, String[] array2) {
//		String wordX, wordY;
//		double matrizResultados[][] = new double[array1.length][array2.length];
//		int arrayIndices[] = new int[array1.length];
//		double arrayResultados[] = new double[array1.length];
//		double distancia = 0;
//
//		for (int i = 0; i < array1.length; i++) {
//			wordX = array1[i];
//			for (int j = 0; j < array2.length; j++) {
//				wordY = array2[j];
//				matrizResultados[i][j] = compute(wordX, wordY);
//			}
//		}
		
		double[][] result = new double[array1.length][array2.length];
		
		RelatednessCalculator rc = new WuPalmer(db);
		for (int i = 0; i < array1.length; i++) {
			for (int j = 0; j < array2.length; j++) {
				double score = rc.calcRelatednessOfWords(array1[i], array2[j]);
				result[i][j] = score;
			}
		}
		

		return resultadoMatriz(result);
	}

	private static double compararCompuesto2(String[] array1, String[] array2) {
		String wordX, wordY;
		double matrizResultados[][] = new double[array1.length][array2.length];
		int arrayIndices[] = new int[array1.length];
		double arrayResultados[] = new double[array1.length];
		double distancia = 0;

		for (int i = 0; i < array1.length; i++) {
			wordX = array1[i];
			for (int j = 0; j < array2.length; j++) {
				wordY = array2[j];
				matrizResultados[i][j] = compute(wordX, wordY);
			}
		}

		// procesar resultados

		bestSolution(matrizResultados);

		return distancia;
	}

	public static void bestSolution(double[][] distances) {

		double[] bestSolution = new double[distances.length];

		for (int i = 0; i < distances.length; i++) {
			double[] parcialDistances = distances[i];

			for (int j = 0; j < parcialDistances.length; j++) {

			}
		}

		// ArrayUtils.removeElement(bestSolution, element);

	}

	public static void getHigherIndex(double[] partialDistances, int[] partialSolutions, int limit) {
		int higherIndex = 0;
		double higherValue = partialDistances[0];

		for (int i = 0; i < partialDistances.length; i++) {
			if (higherValue < partialDistances[i]) {
				higherValue = partialDistances[i];
				higherIndex = i;
			}
		}

		if (ArrayUtils.contains(partialSolutions, higherIndex)) {
			higherValue = 0.0;
			for (int i = higherIndex; i < partialDistances.length; i++) {
				if (higherValue < partialDistances[i]) {
					higherValue = partialDistances[i];
					higherIndex = i;
				}
			}
		}

		// si la solucion contiene dicho indice
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 long startTime = System.currentTimeMillis();
		 procesar();
		 long stopTime = System.currentTimeMillis();
		
		 Date inicio = new Date(startTime);
		 Date fin = new Date(stopTime);
		 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		 System.out.println("Inicio -->" + dateFormat.format(inicio));
		 System.out.println("Fin ->" + dateFormat.format(fin));
		
		 System.out.println("Inicio : ");
		 // System.out.println(compute("Audits", "Administration"));
		 System.out.println("Ejecutado");

//		double[][] matriz = { { 0.64, 0, 9, 0.6 }, { 0.5, 0.7, 0.3 }, { 0.3, 0.8, 0.7 } };
//
//		int[] solP = new int[matriz.length];
//		solF = new int[matriz.length];
//
//		for (int i = 0; i < solP.length; i++) {
//			solP[i] = -1;
//		}
//
//		backtracking2(0, matriz, solP);
//
//		for (int i = 0; i < solP.length; i++) {
//			System.out.print(solF[i] + ", ");
//		}
//
//		System.out.println("FIN");
	}
}
