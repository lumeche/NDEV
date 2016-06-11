package ndev;

import ndev.dictation.Dictation;
import ndev.tts.TextToSpeech;

//TODO: Get arguments from command line
public class TesterBuilder {

	public static NDEVTester getTester(){
//		return new TextToSpeech();
		return new Dictation();
	}
}
