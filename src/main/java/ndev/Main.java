package ndev;

import org.apache.log4j.BasicConfigurator;

public class Main {

	
	public static void main(String[] args) {
		
		NDEVTester tester = TesterBuilder.getTester();
		try {
			tester.runTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
