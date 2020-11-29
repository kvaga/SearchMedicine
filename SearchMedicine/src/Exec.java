import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;

import ru.kvaga.telegram.sendmessage.TelegramSendMessage;


public class Exec {

	// tail -f /proc/`pgrep exec`/fd/2
	// 1 = stdout, 2 = stderr
	
	private static TelegramSendMessage telegramSendMessage;
	
	// Specify a configuration file with token and channelName information
	private static String envFilePath;
	private static String pathFileSitesAndChecks;
	static String token;
	static String channelName;
	private static String[] consoleArguments;
	
	private static void getParameters(String filePath) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(new File(filePath)));
		
		System.out.print(String.format("Reading information from the %s configuration file ... ", filePath));
		token=props.getProperty("token");
		System.out.print(String.format("token=%s ", token));
		channelName=props.getProperty("channelName");
		System.out.println(String.format("channelName=%s ", channelName));
		pathFileSitesAndChecks=props.getProperty("pathFileSitesAndChecks");
		System.out.println(String.format("pathFileSitesAndChecks=%s ", pathFileSitesAndChecks));
	}
	
	public static void main(String[] args) throws Exception {
		usage();
		
		
		
		// Init
		consoleArguments=args;
		if(getConsoleParameterValue("-configFile")!=null) {
			envFilePath=getConsoleParameterValue("-configFile");
		}else {
			envFilePath="conf/TelegramSendMessage.env";
		}
		System.out.println(String.format("Using environment file path %s", envFilePath));

		getParameters(envFilePath);
		
		
		telegramSendMessage = new TelegramSendMessage(token, channelName);
		/*
		String mas[][] = {
				{"https://www.asna.ru/cards/salofalk_4g_60ml_n7_suspenziya_rektalnaya_dr_falk_farma_gmbkh.html","item-page-not-available-text"},
				{"https://www.asna.ru/cards/salofalk_2g_30ml_n7_suspenziya_rektalnaya_dr_falk_farma_gmbkh.html","item-page-not-available-text"},
				{"https://gorzdrav.org/p/salofalk-susp-rekt-4g-60ml-mikroklizma-60ml-n7-9152/","<span class=\"b-stock-status__text\">"},
				{"https://gorzdrav.org/p/salofalk-susp-rekt-2g-30ml-mikroklizma-30ml-n7-9151/","<span class=\"b-stock-status__text\">"},
				{"https://apteka.planetazdorovo.ru/catalog/lekarstva-i-bad/pishchevaritelnyy-trakt/protivovospalitelnye/salofalk-suspenziya-rekt-12380/", "<div class=\"product-detail__warning-no-product\">"},
//				{"https://www.eapteka.ru/goods/id23336/", "<link itemprop=\"availability\" href=\"http://schema.org/OutOfStock\">"},
//				{"https://www.eapteka.ru/goods/id23337/", "<link itemprop=\"availability\" href=\"http://schema.org/OutOfStock\">"},
				{"https://uteka.ru/product/salofalk-30442/", "<button data-test=\"unavailable-button\" type=\"button\" class=\"ui-grid-item product-page-card-offer-actions__unavailable"},
				{"https://uteka.ru/product/salofalk-30444/", "<button data-test=\"unavailable-button\" type=\"button\" class=\"ui-grid-item product-page-card-offer-actions__unavailable"},
				{"https://stolichki.ru/drugs/salofalk-susp-d-rekt-vved-2g-7","<p class=\"badge-class product-not-found\">"},
				{"https://stolichki.ru/drugs/salofalk-susp-d-rekt-vved-4g-7","<p class=\"badge-class product-not-found\">"},
				{"https://megapteka.ru/moskva/catalog/zabolevaniya-zhkt-52/salofalk-suspenziya-rekt-122454","<div class=\"thisNotAvail-action\"><div class=\"thisNotAvail-action-title\">"},
				{"https://megapteka.ru/moskva/catalog/zabolevaniya-zhkt-52/salofalk-susp-rektaln-44108","<div class=\"thisNotAvail-action\"><div class=\"thisNotAvail-action-title\">"},
				{"https://apteka366.ru/p/salofalk-suspenzija-rektal-2g-30ml-mikroklizma-30ml-n7-9151/","<span class=\"b-stock-status__text \">"},
				{"https://apteka366.ru/p/salofalk-suspenzija-rektal-4g-60ml-mikroklizma-60ml-n7-9152/","<span class=\"b-stock-status__text \">"}
		};
		*/
		// Work

//		if(true) {
//			for(int i=0; i<5; i++) {
//				sendMessageToTelegram(""+i);
//			}
//			System.exit(0);
//		}
		
		ArrayList<String[]> al = getSitesAndChecks(pathFileSitesAndChecks);
		telegramSendMessage.sendMessage("Job started");
		for(String m[] : al) {
			try {
				searchMedicine(
						m[0], 
						m[1]
						//new String(m[1].getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
						);
			}catch(Exception e) {
				
				System.err.println(e);
			}
		}
	}

	private static void usage() {
		System.out.println("Usage:");
		System.out.println("-configFile=TelegramSendMessage.env : configuration file path. Default value is TelegramSendMessage.env");
		System.out.println("All other configuration is stored in this config file");
		System.out.println();
		System.out.println();
	}
	
	private static String getConsoleParameterValue(String parameter) {
		parameter=parameter+"=";
		for(String s : consoleArguments) {
			if(s.startsWith(parameter)) {
				parameter=parameter.replaceAll(parameter, "");
				return parameter;
			}
		}
		return null;
	}
	
	private static ArrayList<String[]> getSitesAndChecks(String pathFileSitesAndChecks) throws IOException, SearchMedicineException.NoSitesAndCheckFound{
		File file = new File(pathFileSitesAndChecks);
		ArrayList<String[]> al = new ArrayList<String[]>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String s;
		while((s=br.readLine())!=null) {
			if(s.equals("")) {
				break;
			}
			String mas[]=s.split(",");
			al.add(mas);
		}
		if(al.size()==0) {
			throw new SearchMedicineException.NoSitesAndCheckFound(String.format("Couldn't find any sites and checks in the %s file", pathFileSitesAndChecks));
		}
		return al;
	}
	
	private synchronized static void sendMessageToTelegram(String message) throws Exception {
//		System.err.println("Message: " + message);

		// here send to telegram
		System.out.print("Sending message ... ");
		telegramSendMessage.sendMessage(message);
		System.out.println("[OK]. The message was sent");
		
	}

	public static void searchMedicine(String urlText, String searchNegativeString) throws Exception  {

		try {
			URL url = new URL(urlText);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String s;
			StringBuilder sb = new StringBuilder();
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}

			String response = sb.toString();
			if (response.contains(searchNegativeString)) {
				throw new SearchMedicineException.MedcineIsOutOfStock(
						String.format("Couldn't find a medicine for the URL [%s]. The response contains [%s] sentence", urlText, searchNegativeString));
			}
//			System.out.println(response);
			System.out.println("Found item in URL: " + urlText);
			sendMessageToTelegram("Found item in URL: " + urlText);
		} catch (Exception e) {
			throw new Exception(String.format("Exception during requesting the URL: %s. ", urlText) + e.getMessage());
		}
	}

}
