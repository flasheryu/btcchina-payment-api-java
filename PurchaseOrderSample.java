import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

public class PurchaseOrderSample {
 
	private String ACCESS_KEY="<YOUR PAYMENT ACCESS KEY>";
	private String SECRET_KEY="<YOUR PAYMENT SECRET KEY>";
	private String apiUrl="https://api.btcchina.com/api.php/payment"; 
	
	private static String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private static Logger logger = Logger.getLogger(PurchaseOrderSample.class.getName());
	
	public static void main(String[] args) {
		PurchaseOrderSample sample = new PurchaseOrderSample();
		try{
//			String HTTP_X_BTCCHINA_SIGNATURE="d85675ed4b053c8f356ca81627129fb33a63cc3a"; // get this string from our call back response
//			String body = new String ("{\"id\":\"635\",\"url\":\"https:\\/\\/api.btcchina.com\\/purchaseorder\\/pay?token=51262b0cde531c83b89c806639b38d2137291f26\",\"externalKey\":\"testgq063\",\"price\":\"0.00030000\",\"currency\":\"BTC\",\"btcPrice\":\"0.00030000\",\"creationTime\":1408608533,\"expirationTime\":1408609433,\"status\":\"unconfirmed\",\"itemDesc\":\"A notebook maybe\",\"settlementType\":\"0\"}");
//		    if(sample.callbackVerification(body, HTTP_X_BTCCHINA_SIGNATURE))	System.out.println("Verification Succeeds!");
//		    else	System.out.println("Verification Failed!"); 
		    
			sample.createPurchaseOrder(0.5, "CNY", "<YOUR SERVER URL to PROCESS CALLBACK>","http://www.baidu.com","demo001","A notebook maybe","13500000001",0);
//			sample.createPurchaseOrder(0.0005, "BTC", "http://<YOUR SERVER URL to PROCESS CALLBACK>","<RETURN URL>","demo002","A notebook maybe","13500000001",0);
//			sample.getPurchaseOrder(1);
//			sample.getPurchaseOrders();
			
		} catch ( Exception e){
			logger.log(Level.SEVERE, "Exception found!! : {0}", e.toString());
			return;
		}
	}
	
	public static String getSignature(String data,String key) throws Exception {
		// get an hmac_sha1 key from the raw key bytes
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		// get an hmac_sha1 Mac instance and initialize with the signing key
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		// compute the hmac on input data bytes
		byte[] rawHmac = mac.doFinal(data.getBytes());
		return bytArrayToHex(rawHmac);
	}
 
	private static String bytArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for(byte b: a)
			sb.append(String.format("%02x", b&0xff));
		return sb.toString();
	}

	private static String removeZero(String s){  
        if(s.indexOf(".") > 0){  
            s = s.replaceAll("0+?$", "");//remove redundant 0  
            s = s.replaceAll("[.]$", "");//if the last is ., remove  
        }  
        return s;  
	}
	
	public String sha1(String s) throws Exception{
	    MessageDigest sha1 = MessageDigest.getInstance("SHA1");
	    sha1.update(s.getBytes());
        byte[] hash = sha1.digest();
        return bytArrayToHex(hash);
	}
	
	public boolean callbackVerification(String body, String HTTP_X_BTCCHINA_SIGNATURE) throws Exception{
//		String HTTP_X_BTCCHINA_SIGNATURE="d85675ed4b053c8f356ca81627129fb33a63cc3a";
		body+=SECRET_KEY;
		return (sha1(body).equals(HTTP_X_BTCCHINA_SIGNATURE))?true:false;
	}

	public String createPurchaseOrder(double price, String currency, String notificationUrl, String returnUrl, String externalKey, String itemDesc, String phoneNumber, int settlementType ) throws Exception{

		String tonce = ""+(System.currentTimeMillis() * 1000);
		
		BigDecimal tmp = new BigDecimal(new Double(price).toString());	
		String param_price = removeZero(tmp.toPlainString());
		
		String param_settlementType = removeZero(new Integer(settlementType).toString());
	
		String params = "tonce="+tonce.toString()+"&accesskey="+ACCESS_KEY+"&requestmethod=post&id=1&method=createPurchaseOrder&params="+param_price+","+currency+","+notificationUrl+","+returnUrl+","+externalKey+","+itemDesc+","+phoneNumber+","+param_settlementType; //
		String hash = getSignature(params, SECRET_KEY);
		 
		System.out.println("Params: "+ params);
		String userpass = ACCESS_KEY + ":" + hash;
		String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
		
		URL obj = new URL(apiUrl);
	    HttpsURLConnection httpsConn = (HttpsURLConnection)obj.openConnection();;
	    
	    //add reuqest header
	    httpsConn.setRequestMethod("POST");
	    httpsConn.setRequestProperty("Json-Rpc-Tonce", tonce.toString());
	    httpsConn.setRequestProperty ("Authorization", basicAuth);
	 
		String postdata = "{\"method\": \"createPurchaseOrder\", \"params\": ["+param_price+",\""+currency+"\",\""+notificationUrl+"\",\""+returnUrl+"\",\""+externalKey+"\",\""+itemDesc+"\",\""+phoneNumber+"\","+param_settlementType+"], \"id\": 1}";
	 
		// Send post request
		httpsConn.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(httpsConn.getOutputStream());
		wr.writeBytes(postdata);
		wr.flush();
		wr.close();
	 
		logger.log(Level.INFO, "Post parameters : {0}", params);
		logger.log(Level.INFO, "Post parameters : {0}", postdata);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(httpsConn.getInputStream()));
		
		String inputLine;
		StringBuffer response = new StringBuffer();
	
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	
		logger.log(Level.WARNING, "Response : {0}", response.toString());

		return response.toString();
	}
	
	public String getPurchaseOrder(int order_id) throws Exception{

		String tonce = ""+(System.currentTimeMillis() * 1000);
		
		String param_order_id = removeZero(new Integer(order_id).toString());

		String params = "tonce="+tonce.toString()+"&accesskey="+ACCESS_KEY+"&requestmethod=post&id=1&method=getPurchaseOrder&params="+param_order_id; //
		String hash = getSignature(params, SECRET_KEY);
		System.out.println(params); 
		
		String userpass = ACCESS_KEY + ":" + hash;
		String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
		
		URL obj = new URL(apiUrl);
	    HttpsURLConnection httpsConn = (HttpsURLConnection)obj.openConnection();;
		
	    //add reuqest header
	    httpsConn.setRequestMethod("POST");
	    httpsConn.setRequestProperty("Json-Rpc-Tonce", tonce.toString());
	    httpsConn.setRequestProperty ("Authorization", basicAuth);
	 
		String postdata = "{\"method\": \"getPurchaseOrder\", \"params\": ["+param_order_id+"], \"id\": 1}";
	 
		// Send post request
		httpsConn.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(httpsConn.getOutputStream());
		wr.writeBytes(postdata);
		wr.flush();
		wr.close();
	 
		logger.log(Level.INFO, "Post parameters : {0}", params);
		logger.log(Level.INFO, "Post parameters : {0}", postdata);
				
		BufferedReader in = new BufferedReader(new InputStreamReader(httpsConn.getInputStream()));
		
		String inputLine;
		StringBuffer response = new StringBuffer();
	
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	
		logger.log(Level.WARNING, "Response : {0}", response.toString());

		return response.toString();
	}	
	
	
	public String getPurchaseOrders(int limit, int offset, int fromDate, int toDate, String status, String externalKey) throws Exception{
		String tonce = ""+(System.currentTimeMillis() * 1000);
		
		String param_limit = removeZero(new Integer(limit).toString());
		String param_offset = removeZero(new Integer(offset).toString());
		String param_fromDate = removeZero(new Integer(fromDate).toString());
		String param_toDate = removeZero(new Integer(toDate).toString());

		String params = "tonce="+tonce.toString()+"&accesskey="+ACCESS_KEY+"&requestmethod=post&id=1&method=getPurchaseOrders&params="+param_limit+","+param_offset+","+param_fromDate+","+param_toDate+","+status+","+externalKey; //
		String hash = getSignature(params, SECRET_KEY);
		System.out.println(params); 
 
		String userpass = ACCESS_KEY + ":" + hash;
		String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
		
		URL obj = new URL(apiUrl);

		HttpsURLConnection httpsConn = (HttpsURLConnection) obj.openConnection();
	    
	  //add reuqest header
	    httpsConn.setRequestMethod("POST");
	    httpsConn.setRequestProperty("Json-Rpc-Tonce", tonce.toString());
	    httpsConn.setRequestProperty ("Authorization", basicAuth);
	 
		String postdata = "{\"method\": \"getPurchaseOrders\", \"params\": ["+param_limit+","+param_offset+","+param_fromDate+","+param_toDate+",\""+status+"\",\""+externalKey+"\"], \"id\": 1}";
	 
		// Send post request
		httpsConn.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(httpsConn.getOutputStream());
		wr.writeBytes(postdata);
		wr.flush();
		wr.close();
	 
		logger.log(Level.INFO, "Post parameters : {0}", params);
		logger.log(Level.INFO, "Post parameters : {0}", postdata);
				
		BufferedReader in = new BufferedReader(new InputStreamReader(httpsConn.getInputStream()));
		
		String inputLine;
		StringBuffer response = new StringBuffer();
	
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	
		logger.log(Level.WARNING, "Response : {0}", response.toString());

		return response.toString();
	}
	
	public String getPurchaseOrders() throws Exception{
		String tonce = ""+(System.currentTimeMillis() * 1000);
		
		String params = "tonce="+tonce.toString()+"&accesskey="+ACCESS_KEY+"&requestmethod=post&id=1&method=getPurchaseOrders&params="; //
		String hash = getSignature(params, SECRET_KEY);
		System.out.println(params); 
 
		String userpass = ACCESS_KEY + ":" + hash;
		String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
		
		URL obj = new URL(apiUrl);

		HttpsURLConnection httpsConn = (HttpsURLConnection) obj.openConnection();
	    
	  //add reuqest header
	    httpsConn.setRequestMethod("POST");
	    httpsConn.setRequestProperty("Json-Rpc-Tonce", tonce.toString());
	    httpsConn.setRequestProperty ("Authorization", basicAuth);
	 
		String postdata = "{\"method\": \"getPurchaseOrders\", \"params\": [], \"id\": 1}";
	 
		// Send post request
		httpsConn.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(httpsConn.getOutputStream());
		wr.writeBytes(postdata);
		wr.flush();
		wr.close();
	 
		logger.log(Level.INFO, "Post parameters : {0}", params);
		logger.log(Level.INFO, "Post parameters : {0}", postdata);
				
		BufferedReader in = new BufferedReader(new InputStreamReader(httpsConn.getInputStream()));
		
		String inputLine;
		StringBuffer response = new StringBuffer();
	
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	
		logger.log(Level.WARNING, "Response : {0}", response.toString());

		return response.toString();
	}
}
