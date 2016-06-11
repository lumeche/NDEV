package ndev.config;
//TODO: Move this to a property file

public class Config {
	public static String APP_ID = "NMDPTRIAL_lumeche_hotmail_com20160601074225";
	public static String APP_KEY = "8116135eeec700b7ca236f9e1b9457c87c2babdaab2d54ec6ae6efd99c3c061e52197c40e17df7aba06649bbfaa18376cc2e50775ca463c68b529f278ea545e0";
	public static String DEVICE_ID = "0000";
	
	public static String VOICE = "Samantha";
	public static String LANGUAGE = "en_US";
	public static String CODEC = "audio/x-wav;codec=pcm;bit=16;rate=22000";	//MP3
	
	public static String TEXT = "Hello World. I'm testing this application.";

	public static short PORT = (short) 443;
	public static String HOSTNAME = "tts.nuancemobility.net";
	
//TTS Configs
	public static String TTS_SERVER_PATH = "/NMDPTTSCmdServlet/tts";
	public static String TTS_FILE="./tts_audio.pcm"; 

//Dictation Config
	public static String DICTATION_SERVER_PATH = "/NMDPAsrCmdServlet/dictation";
	public static int SAMPLE_RATE = 16000;
	public static String DICTATION_RESULT_PATH="./dictationResult.txt";
	public static String RESULTS_FORMAT = "text/plain";
	public static String TOPIC = "Dictation";	// or WebSearch
	
}
