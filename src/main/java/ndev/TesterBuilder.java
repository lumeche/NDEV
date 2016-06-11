package ndev;

import ndev.dictation.Dictation;

public class TesterBuilder {

	public static NDEVTester getTester(){
		return new Dictation();
	}
}
