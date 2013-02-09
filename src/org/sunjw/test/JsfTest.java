package org.sunjw.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.sunjw.js.JsfFile;

public class JsfTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: jsf [input file] [output file]");
			return;
		}

		String inputFile = args[0];
		String outputFile = args[1];

		FileReader fr;
		FileWriter fw;
		try {
			fr = new FileReader(inputFile);
			fw = new FileWriter(outputFile);
			JsfFile jff = new JsfFile(fr, fw, '\t', 1, false, false);
			jff.debugOutput = true;
			jff.go();
			fr.close();
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
