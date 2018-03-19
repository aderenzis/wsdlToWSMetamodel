package edu.uncoma.fai.WsdlToSoaML.parser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

import edu.giisco.SoaML.metamodel.Interface;
public class mainWsdlToSoaML {

	public static void main (String [] args)
	{
		testWSDL20toSoaML("C:\\pruebasEclipse\\Convertidos\\");
	}

	public static void testWSDL20toSoaML(String path) {
		// Creacion de archivo resultado.txt
		File fResult;
		fResult = new File("C:\\Users\\lenovo1\\Documents\\WSDLtoSOAML20160530.txt");
		// Escritura
		try {
			FileWriter w = new FileWriter(fResult);
			BufferedWriter bw = new BufferedWriter(w);
			PrintWriter wr = new PrintWriter(bw);
			wr.write("");// escribimos en el archivo
			// Directorio que vas a analizar.. El que contiene los documentos WSDL
			String pathConvertedTo20= path;
			File dir = new File(pathConvertedTo20);
			String[] files = dir.list();
			if (files == null)
				System.out.println("No files into the specified folder");
			else {
				int countErrors = 0;
				File[] fileList = dir.listFiles();

				for (int i = 0; i < fileList.length; i++) {
					File fileAux = fileList[i];

					System.out.println("Analizando " + i + "  de  "
							+ fileList.length + "  --- Errors: " + countErrors
							+ " -- " + fileAux.getName());
					try {
						String fileName = fileAux.getName();
						//Interface soaMLInterface = WsdlToSoaML.createSoaMLInterface(pathConvertedTo20+"yotpo.wsdl2");
//						Interface soaMLInterface = WsdlToSoaML.createSoaMLInterface(pathConvertedTo20+"yourmapper.wsdl2");
						Interface soaMLInterface = WsdlToSoaML.createSoaMLInterface(pathConvertedTo20+fileName);
						wr.append(soaMLInterface.toString());
						wr.append("\n\n--------------------------------------------------------------------------------------\n\n");
					}
					catch (Exception e) {
						e.printStackTrace();
						countErrors++;
						wr.append(fileAux.getName() + "  -  "+ e.getStackTrace() + "\n");
					}
				}
			}
			wr.close();
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}