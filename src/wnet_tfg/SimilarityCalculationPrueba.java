package wnet_tfg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

public class SimilarityCalculationPrueba {

	private final String FILENAME1 = "input2/themes_all.txt";
	private final String FILENAME2 = "input2/categories.txt";
	private final String FILENAME3 = "input2/identifier.txt";

	// private final String FILENAME1 = "input2/temasPruebas.txt";
	// private final String FILENAME2 = "input2/categoriasPruebas.txt";

	private ILexicalDatabase db = new NictWordNet();

	private Connection conn;

	public int contador = 0;

	public void connect() {

		if (conn == null) {
			try {
				// db parameters
				Class.forName("org.sqlite.JDBC");
				String url = "jdbc:sqlite:db/OpenDataCatalogs.sqlite";
				// create a connection to the database
				conn = DriverManager.getConnection(url);

				System.out.println("Connection to SQLite has been established.");

			} catch (SQLException e) {
				System.out.println(e.getMessage());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// conn = null;

		/*
		 * try { // db parameters Class.forName("org.sqlite.JDBC"); String url =
		 * "jdbc:sqlite:db/OpenDataCatalogs.sqlite"; // create a connection to
		 * the database conn = DriverManager.getConnection(url);
		 * 
		 * System.out.println("Connection to SQLite has been established.");
		 * 
		 * } catch (SQLException e) { System.out.println(e.getMessage()); }
		 * catch (ClassNotFoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } // finally { // try { // if (conn != null) {
		 * // conn.close(); // } // } catch (SQLException ex) { //
		 * System.out.println(ex.getMessage()); // } // }
		 */
	}

	public void disconnect() {
		try {
			if (conn != null) {
				conn.close();
				System.out.println("Connection to SQLite has been closed.");
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}
	}

	private double similitud(String p1, String p2) {

		double similitud = 0;
		double similitudParcial = 0;
		int contador = 0;

		String[] words1 = p1.split(" ");
		String[] words2 = p2.split(" ");

		// jaro-winkler

		SimilarityStrategy strategy = new JaroWinklerStrategy();
		StringSimilarityService service = new StringSimilarityServiceImpl(strategy);

		for (int i = 0; i < words1.length; i++) {
			for (int j = 0; j < words2.length; j++) {
				similitudParcial = service.score(words1[i], words2[j]);
				if (similitudParcial > 0) {
					similitud += similitudParcial;
					contador++;
				}
			}
		}

		// ws4j

		// RelatednessCalculator rc1 = new WuPalmer(db);
		// double[][] s1 = getSimilarityMatrix(words1, words2, rc1);
		// for (int i = 0; i < words1.length; i++) {
		// for (int j = 0; j < words2.length; j++) {
		// if (s1[i][j] > 0.0) {
		// similitud += s1[i][j];
		// contador++;
		// }
		// }
		// }

		return similitud / contador;
	}

	public void similitudMensaje(String p1, String p2) {

		double similitud = 0;
		int contador = 0;

		System.out.println("WuPalmer");

		String[] words1 = p1.split(" ");
		String[] words2 = p2.split(" ");
		RelatednessCalculator rc1 = new WuPalmer(db);
		{
			double[][] s1 = getSimilarityMatrix(words1, words2, rc1);
			for (int i = 0; i < words1.length; i++) {
				for (int j = 0; j < words2.length; j++) {

					if (s1[i][j] > 0.0) {
						contador++;
					}
					similitud += s1[i][j];
					System.out.print(s1[i][j] + "\t");
				}
				System.out.println();
			}
		}
		System.out.println();
		System.out.println();

		System.out.println("Total --> " + similitud / contador);

	}

	private void escribirResultado(String nombreFichero, String palabra, double resultado) {
		FileWriter fichero = null;
		PrintWriter pw = null;

		try {

			fichero = new FileWriter("salida/" + nombreFichero + ".txt", true);
			pw = new PrintWriter(fichero);

			pw.println(palabra + " - " + resultado);

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

	public void procesar() {
		BufferedReader br1, br2, br3 = null;
		FileReader fr1 = null;
		FileReader fr2 = null;
		FileReader fr3 = null;
		String tema, titulo, palabraProcesada, temaActual, categoriaActual;
		ArrayList<String> categorias = new ArrayList<String>();
		ArrayList<String> temas = new ArrayList<String>();
		ArrayList<String> identificadores = new ArrayList<String>();
		double[] resultadoParcial;
		double distancia;
		int errores = 0;
		int[] arrayResultados = new int[15];

		for (int i = 0; i < arrayResultados.length; i++) {
			arrayResultados[i] = 0;
		}

		try {

			String currentLine1, currentLine2, currentLine3, mejorCategoria;

			// abro fichero themes_all.txt
			fr1 = new FileReader(FILENAME1);

			// abro fichero categories.txt
			br1 = new BufferedReader(fr1);

			fr2 = new FileReader(FILENAME2);
			br2 = new BufferedReader(fr2);

			fr3 = new FileReader(FILENAME3);
			br3 = new BufferedReader(fr3);

			while ((currentLine2 = br2.readLine()) != null) {
				categorias.add(currentLine2);
			}

			while ((currentLine1 = br1.readLine()) != null) {
				temas.add(currentLine1);
			}

			while ((currentLine3 = br3.readLine()) != null) {
				identificadores.add(currentLine3);
			}

			// for (int i = 0; i < identificadores.size(); i++) {
			// update(identificadores.get(i), "usa_city_datasets_categorized",
			// "");
			// }

			boolean esIgual = false;

			for (int i = 0; i < temas.size(); i++) {
				temaActual = temas.get(i);

				resultadoParcial = new double[categorias.size()];

				int contador = 0;
				int idCategoriaActual = 0;

				String[] temaPartes = temaActual.split(" ");
				esIgual = false;
				for (int j = 0; j < categorias.size(); j++) {
					categoriaActual = categorias.get(j);
					// for (int k = 0; k < temaPartes.length; k++) {
					// if ((!temaPartes[k].toLowerCase().equals("and") &&
					// !temaPartes[k].toLowerCase().equals("")
					// && !temaPartes[k].equals(",")
					// &&
					// categoriaActual.toLowerCase().contains(temaPartes[k].toLowerCase()))
					// || (!temaPartes[k].toLowerCase().equals("and")
					// && !temaPartes[k].toLowerCase().equals("") &&
					// !temaPartes[k].equals(",")
					// &&
					// categoriaActual.toLowerCase().contains(temaPartes[k].toLowerCase()
					// + ","))) {
					// System.out.println("Entra - " + temaActual);
					// escribirResultado(categoriaActual, temaActual, 1);
					// esIgual = true;
					// }
					// }
					//
					for (int k = 0; k < temaPartes.length && !esIgual; k++) {
						if ((!temaPartes[k].toLowerCase().equals("and") && !temaPartes[k].toLowerCase().equals("")
								&& !temaPartes[k].equals(",")
								&& categoriaActual.toLowerCase().contains(temaPartes[k].toLowerCase()))
								|| (!temaPartes[k].toLowerCase().equals("and")
										&& !temaPartes[k].toLowerCase().equals("") && !temaPartes[k].equals(",")
										&& categoriaActual.toLowerCase().contains(temaPartes[k].toLowerCase() + ","))) {
							// System.out.println("Entra - " + temaActual);
							arrayResultados[j]++;
							esIgual = true;
							update(identificadores.get(i), "usa_city_datasets_categorized", categoriaActual);
						}
					}

					if (!esIgual) {

						distancia = similitud(temaActual, categoriaActual);

						// System.out.println("Palabra " + contador);
						// if (!categoriaActual.equals("")) {
						// // si categoria y tema son palabras compuestas
						// if (temaActual.contains(" ") &&
						// categoriaActual.contains(" ")) {
						//
						// String[] array1 = temaActual.split(" ");
						// String[] array2 = categoriaActual.split(" ");
						// distancia = compararCompuesto(array1, array2);
						//
						//
						// // si el tema es compuesto y la categoria es simple
						// } else if (temaActual.contains(" ") &&
						// !categoriaActual.contains(" ")) {
						// String[] array1 = temaActual.split(" ");
						// distancia = compararSimpleCompuesto(categoriaActual,
						// array1);
						//
						// // si el tema es simple y la categoria compuesta
						// } else if (!temaActual.contains(" ") &&
						// categoriaActual.contains(" ")) {
						// String[] array2 = categoriaActual.split(" ");
						// distancia = compararSimpleCompuesto(temaActual,
						// array2);
						//
						// // si tema y categoria son simples
						// } else {
						// distancia = similitud(temaActual, categoriaActual);
						//
						// }
						//
						// resultadoParcial[contador] = distancia;
						// }
						resultadoParcial[contador] = distancia;
						idCategoriaActual++;
						contador++;
					}
				}

				// if (!esIgual) {
				// int mejorResultado =
				// comprobarMejorResultado(resultadoParcial);
				//
				// if (resultadoParcial[mejorResultado] >= 0.4) {
				// escribirResultado(categorias.get(mejorResultado), temaActual,
				// resultadoParcial[mejorResultado]);
				// } else {
				// escribirResultado("Otros", temaActual,
				// resultadoParcial[mejorResultado]);
				// errores++;
				// }
				//
				// }

				if (!esIgual) {
					int mejorResultado = comprobarMejorResultado(resultadoParcial);

					if (resultadoParcial[mejorResultado] >= 0.4) {
						arrayResultados[mejorResultado]++;

						mejorCategoria = selectCategoria(mejorResultado);

						update(identificadores.get(i), "usa_city_datasets_categorized", mejorCategoria);

					} else {
						arrayResultados[arrayResultados.length - 1]++;
						// actualizar base de datos
						// usa_city_datasets_categorized

						// select();

						update(identificadores.get(i), "usa_city_datasets_categorized", "Others");

					}

				}

				// System.out.println("Palabra " + i);

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
		// System.out.println("Errores : " + errores);

		mostrarResultados(arrayResultados);

	}

	public String selectCategoria(int indice) {
		String tema;
		switch (indice) {
		case 0:
			tema = "Administration & Finance";
			break;
		case 1:
			tema = "Business";
			break;
		case 2:
			tema = "Demographics";
			break;
		case 3:
			tema = "Education";
			break;
		case 4:
			tema = "Ethics & Democracy";
			break;
		case 5:
			tema = "Geospatial";
			break;
		case 6:
			tema = "Health";
			break;
		case 7:
			tema = "Recreation & Culture";
			break;
		case 8:
			tema = "Safety";
			break;
		case 9:
			tema = "Services";
			break;
		case 10:
			tema = "Sustainability";
			break;
		case 11:
			tema = "Transport & Infrastructure";
			break;
		case 12:
			tema = "Urban Planning & Housing";
			break;
		case 13:
			tema = "Welfare";
			break;
		default:
			tema = "Others";
			break;
		}

		return tema;
	}

	public void update(String id, String tableName, String name) {
		String sql = "UPDATE usa_city_datasets_categorized SET Category = ? WHERE identifier = ?";

		try {
			PreparedStatement pstmt = conn
					.prepareStatement("UPDATE usa_city_datasets_categorized SET Category = ? WHERE identifier = ?");
			// set the corresponding param
			// pstmt.setString(1, tableName);
			pstmt.setString(1, name);
			pstmt.setString(2, id);
			// update
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void select() {
		ResultSet result = null;
		try {
			PreparedStatement st = conn.prepareStatement("select name  from sqlite_master where type = 'table'");
			result = st.executeQuery();
			while (result.next()) {
				System.out.print("name: ");
				System.out.println(result.getString("name"));
				//
				// System.out.print("Nombre: ");
				// System.out.println(result.getString("nombre"));
				//
				// System.out.print("Apellidos: ");
				// System.out.println(result.getString("apellidos"));

				System.out.println("=======================");
			}
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void mostrarResultados(int[] arrayResultados) {
		int contador = 0;
		for (int i = 0; i < arrayResultados.length; i++) {
			switch (i) {
			case 0:
				System.out.println("Administration and Finance" + " - " + arrayResultados[i]);
				break;
			case 1:
				System.out.println("Business" + " - " + arrayResultados[i]);
				break;
			case 2:
				System.out.println("Demography" + " - " + arrayResultados[i]);
				break;
			case 3:
				System.out.println("Education" + " - " + arrayResultados[i]);
				break;
			case 4:
				System.out.println("Ethics and Democracy" + " - " + arrayResultados[i]);
				break;
			case 5:
				System.out.println("Geospatial" + " - " + arrayResultados[i]);
				break;
			case 6:
				System.out.println("Health" + " - " + arrayResultados[i]);
				break;
			case 7:
				System.out.println("Recreation and Culture" + " - " + arrayResultados[i]);
				break;
			case 8:
				System.out.println("Safety" + " - " + arrayResultados[i]);
				break;
			case 9:
				System.out.println("Services" + " - " + arrayResultados[i]);
				break;
			case 10:
				System.out.println("Sustainability" + " - " + arrayResultados[i]);
				break;
			case 11:
				System.out.println("Transport and Infrastructure" + " - " + arrayResultados[i]);
				break;
			case 12:
				System.out.println("Urban Planning and Housing" + " - " + arrayResultados[i]);
				break;
			case 13:
				System.out.println("Welfare" + " - " + arrayResultados[i]);
				break;
			case 14:
				System.out.println("Otros" + " - " + arrayResultados[i]);
				break;
			default:
				break;
			}
			contador += arrayResultados[i];
		}
		System.out.println("Total -->" + contador);
	}

	private double compararSimpleCompuesto(String palabra, String[] arrayPalabras) {
		double resultado = 0;

		for (int i = 0; i < arrayPalabras.length; i++) {
			resultado += similitud(palabra, arrayPalabras[i]);
		}

		resultado = resultado / arrayPalabras.length;
		return resultado;
	}

	private double compararCompuesto(String[] arrayPalabras1, String[] arrayPalabras2) {
		double resultado = 0;
		int longitud = arrayPalabras1.length + arrayPalabras2.length;

		for (int i = 0; i < arrayPalabras1.length; i++) {
			for (int j = 0; j < arrayPalabras2.length; j++) {
				resultado += similitud(arrayPalabras1[i], arrayPalabras2[j]);
			}
		}

		resultado = resultado / longitud;

		return resultado;
	}

	private int comprobarMejorResultado(double resultadoParcial[]) {
		double mayor = resultadoParcial[0];
		int indice = 0;

		for (int i = 0; i < resultadoParcial.length; i++) {
			if (resultadoParcial[i] > mayor) {
				indice = i;
				mayor = resultadoParcial[i];
			}
		}

		return indice;

	}

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

	public void compute(String frase1, String frase2) {
		System.out.println("WuPalmer");

		String[] words1 = frase1.split(" ");
		String[] words2 = frase2.split(" ");
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
		System.out.println();
		System.out.println();
	}

	public void crearVistas() {
		ResultSet result = null;
		try {
			
			//String query1 ="SELECT name FROM sqlite_master WHERE type='view' ORDER BY name;"; 
					
			PreparedStatement st = conn.prepareStatement("select name  from sqlite_master where type = 'view'");
			result = st.executeQuery();
			while (result.next()) {
//				System.out.print("name: ");
//				System.out.println(result.getString("name"));
				
//				if(result.getString("name").equals("repository_useful_data_for_indicators")){
//					
//					PreparedStatement st1 = conn.prepareStatement("drop view repository_useful_data_for_indicators");
//					
//					st1.executeQuery();
//					
////					if(borrada){
////						System.err.println("BORRADA");
////					}else{
////						System.err.println("NO BORRADA");
////					}
//					
//					
//					//System.err.println("ENCONTRADA");
//				}
				

			}
//			
//			ResultSet rs = preparedStatement.executeQuery(selectSQL );
//			PreparedStatement pstmt = conn.prepareStatement(
//					"create view repository_useful_data_for_indicators as select distinct repository_id,total_contributors,total_contributions,subscribers_count,created_at,updated_at from measures_repository where  created_at!='' order by repository_id");
//			
//			boolean exito = pstmt.execute();
//			if(exito){
//				System.out.println("Vista creada correctamente");
//			}else{
//				System.out.println("Vista NO CREADA");
//			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// procesar();

		SimilarityCalculationPrueba s = new SimilarityCalculationPrueba();

		s.connect();

		s.procesar();

		s.crearVistas();

		s.disconnect();

		// similitudMensaje("Transportation","Transport & Infrastructure");
	}

}
