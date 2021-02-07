import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

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
			String response=null;

			if(!urlText.contains("uteka")) {
				response = getURLContent(urlText);
			}else {
				response = getURLContentSimple(urlText);
			}
			System.err.println("Response (first 25 symbols) for url ["+urlText+"]:" + response.substring(0,25));

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

	public static String getURLContentSimple(String urlText) throws IOException {
		URL url = new URL(urlText);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String s;
		StringBuilder sb = new StringBuilder();
		while ((s = br.readLine()) != null) {
			sb.append(s);
		}
		return sb.toString();
	}
	public static String getURLContent(String urlText) throws Exception {
		String body = null;
		String charset; // You should determine it based on response header.
		HttpURLConnection con=null;

		try {
			URL url = new URL(urlText);
			con = (HttpURLConnection) url.openConnection();
//			con.connect();
			

//			System.out.println("Con: " + con.getResponseCode());
			con.setRequestMethod("GET");
			con.setRequestProperty("accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
			con.setRequestProperty("accept-encoding", "gzip, deflate, br");
			con.setRequestProperty("accept-language", "en-GB,en;q=0.9,ru-RU;q=0.8,ru;q=0.7,en-US;q=0.6");
			con.setRequestProperty("cache-control", "max-age=0");
			con.setRequestProperty("sec-ch-ua",
					"\"Google Chrome\";v=\"87\", \" Not;A Brand\";v=\"99\", \"Chromium\";v=\"87\"");
			con.setRequestProperty("sec-ch-ua-mobile", "?0");
			con.setRequestProperty("sec-fetch-dest", "document");
			con.setRequestProperty("sec-fetch-mode", "navigate");
			con.setRequestProperty("sec-fetch-site", "none");
			con.setRequestProperty("sec-fetch-user", "?1");
			con.setRequestProperty("upgrade-insecure-requests", "1");
			con.setRequestProperty("user-agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");

//			System.out.println("Connection response code: " + con.getResponseCode());

			if (con.getContentType().toLowerCase().contains("charset=utf-8")) {
				charset = "UTF-8";
			} else {
				throw new Exception(urlText + "" + String.format(". Received unsupported charset: %s. ", con.getContentType()));
			}
			if (con.getContentEncoding().equals("gzip")) {
				try (InputStream gzippedResponse = con.getInputStream();
						InputStream ungzippedResponse = new GZIPInputStream(gzippedResponse);
						Reader reader = new InputStreamReader(ungzippedResponse, charset);
						Writer writer = new StringWriter();) {
					char[] buffer = new char[10240];
					for (int length = 0; (length = reader.read(buffer)) > 0;) {
						writer.write(buffer, 0, length);
					}
					body = writer.toString();
					writer.close();
//				    System.err.println(body);
				}
//				System.out.println("Received gzip content for url ["+urlText+"]: " + body.substring(0,25));

			} else {
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String s;
//				System.err.println("Response Message: " + con.getContentEncoding());
				StringBuilder sb = new StringBuilder();
				while ((s = br.readLine()) != null) {
//					System.out.println(s);
					sb.append(s);
				}
				body = sb.toString();
				br.close();
//				System.out.println("Received plain text content for url ["+urlText+"]: " + body.substring(0, 25));

			}
			con.disconnect();

			return body;
			
		} catch (Exception e) {
//			e.printStackTrace();
			System.err.println("GetURLContentException: couldn't get a content for the ["+urlText+"] URL:" + e);
			if(con!=null) {
				con.disconnect();
			}
			throw new Exception(urlText,e);
		}
	}
}
