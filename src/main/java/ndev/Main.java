package ndev;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Main {
	private Logger logger = LoggerFactory.getLogger(Main.class);
	
	@Option(name="--nmaid",required=true)
	private String nmaid;
	@Option(name="--appKey",required=true)
	private String appKey;
	
	private void startApplication(String [] args){
		try {
			CmdLineParser parser=new CmdLineParser(this);
			parser.parseArgument(args);
			logger.info("NMAID:{}",nmaid);
			logger.info("APP_KEYl {}",appKey);
			NDEVTester tester = TesterBuilder.getTester();
			
			try {
				tester.runTest(nmaid,appKey);
			} catch (Exception e) {
				logger.error("Error running test",e);
				
			}
			
		} catch (CmdLineException e) {
			logger.error("Error parsing arguments {}",StringUtils.join(args),e);
		}
		
	}
	
	public static void main(String[] args) {
		new Main().startApplication(args);
	}

	public String getNmaid() {
		return nmaid;
	}

	public void setNmaid(String nmaid) {
		this.nmaid = nmaid;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	
}
