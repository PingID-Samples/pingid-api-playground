package com.pingidentity.developer.pingid;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.jose4j.base64url.Base64;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.pingidentity.developer.pingid.User;


public class Operation {
    
    private static final String ENDPOINT_BASE = "https://idpxnyl3m.pingidentity.com/pingid/rest/4/";
    
	private String name;
	private String endpoint;
	private String requestToken;
	private String responseToken;
	private int responseCode;
	private Boolean wasSuccessful;
	
	private long errorId;
	private String errorMsg;
	private String uniqueMsgId;

	private String orgAlias;
	private String token;
	private String useBase64Key;
	
	private String lastActivationCode;
	private String lastSessionId;
	
	private Map<String, Object> values;
	
	private String clientData;
	private User user;
	
	private final String apiVersion = "4.9";
	
	public Operation() {
	}
	
	public Operation(String orgAlias, String token, String useBase64Key) {
		this.orgAlias = orgAlias;
		this.token = token;
		this.useBase64Key = useBase64Key;
		
		this.values = new HashMap<String, Object>();
	}

	public String getName() { return name; }
	public String getEndpoint() { return endpoint; }
	public String getRequestToken() { return requestToken; }
	public String getResponseToken() { return responseToken; }
    public int getResponseCode() { return responseCode; }
	public Boolean getWasSuccessful() { return wasSuccessful; }
	public String getLastActivationCode() { return this.lastActivationCode; }
	public String getLastSessionId() { return this.lastSessionId; }
	
	public long getErrorId() { return errorId; }
	public String getErrorMsg() { return errorMsg; }
	public String getUniqueMsgId() { return uniqueMsgId; }
	public Map<String, Object> getReturnValues() { return values; }

	public User getUser() { return user; }
	
	public void setTargetUser(User user) { this.user = user; }
	public void setTargetUser(String username) { this.user = new User(username); }
	public void setLastActivationCode(String activationCode) { this.lastActivationCode = activationCode; }
	public void setLastSessionId(String sessionId) { this.lastSessionId = sessionId; }

	// public methods
	@SuppressWarnings("unchecked")
	public void AddUser(Boolean activateUser) {

		this.name = "AddUser";
		this.endpoint = ENDPOINT_BASE + "adduser/do";
		
		JSONObject reqBody = new JSONObject();
		reqBody.put("activateUser", activateUser);
		reqBody.put("email", this.user.getEmail());
		reqBody.put("fName", this.user.getFirstName());
		reqBody.put("lname", this.user.getLastName());
		reqBody.put("username", this.user.getUserName());
		reqBody.put("role", this.user.getRole().getValue());
		reqBody.put("clientData", this.clientData);

		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		JSONObject response = parseResponse();
		values.clear();
		if (activateUser) {
			this.lastActivationCode = (String)response.get("activationCode");
		}
	}

	@SuppressWarnings("unchecked")
	public void EditUser(Boolean activateUser) {

		this.name = "EditUser";
		this.endpoint = ENDPOINT_BASE + "edituser/do";
		
		JSONObject reqBody = new JSONObject();
		reqBody.put("activateUser", activateUser);
		reqBody.put("email", this.user.getEmail());
		reqBody.put("fName", this.user.getFirstName());
		reqBody.put("lName", this.user.getLastName());
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("role", this.user.getRole().getValue());
		reqBody.put("clientData", this.clientData);

		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		JSONObject response = parseResponse();
		values.clear();
		if (activateUser) {
			this.lastActivationCode = (String)response.get("activationCode");
		}
	}

	@SuppressWarnings("unchecked")
	public void GetUserDetails() {

		this.name = "GetUserDetails";
		this.endpoint = ENDPOINT_BASE + "getuserdetails/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("getSameDeviceUsers", false);
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		values.clear();

		JSONObject response = parseResponse();
		JSONObject userDetails = (JSONObject)response.get("userDetails");
		this.user = new User(userDetails);
		
		DeviceDetails deviceDetails = new DeviceDetails();
		List<DeviceDetails> devices = new ArrayList<DeviceDetails>();
		if(userDetails != null) {
		    JSONArray devicesArray = (JSONArray)userDetails.get("devicesDetails");
		    if(devicesArray != null) {
		        for(int i = 0; i < devicesArray.size(); i++) {
		            devices.add(new DeviceDetails((JSONObject)devicesArray.get(i))); 
		        }
		    }
	        deviceDetails = new DeviceDetails((JSONObject)userDetails.get("deviceDetails"));
		}
		this.user.setDeviceDetails(deviceDetails);
		this.user.setDevices(devices);
	}
	
	@SuppressWarnings("unchecked")
	public void DeleteUser() {

		this.name = "DeleteUser";
		this.endpoint = ENDPOINT_BASE + "deleteuser/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		parseResponse();
		values.clear();
	}

	@SuppressWarnings("unchecked")
	public void SuspendUser() {

		this.name = "SuspendUser";
		this.endpoint = ENDPOINT_BASE + "suspenduser/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		parseResponse();
		values.clear();
	}

	@SuppressWarnings("unchecked")
	public void ActivateUser() {

		this.name = "ActivateUser";
		this.endpoint = ENDPOINT_BASE + "activateuser/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		parseResponse();
		values.clear();
	}

	@SuppressWarnings("unchecked")
	public void ToggleUserBypass(long until) {

		this.name = "ToggleUserBypass";
		this.endpoint = ENDPOINT_BASE + "userbypass/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("bypassUntil", (until != 0) ? until : null);
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		parseResponse();
		values.clear();
	}

	@SuppressWarnings("unchecked")
	public void UnpairDevice(long deviceId) {

		this.name = "UnpairDevice";
		this.endpoint = ENDPOINT_BASE + "unpairdevice/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("clientData", this.clientData);
		
		if(deviceId > 0)
			reqBody.put("deviceId", deviceId);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		parseResponse();
		values.clear();
	}

	@SuppressWarnings("unchecked")
	public void GetPairingStatus(String activationCode) {

		this.name = "GetPairingStatus";
		this.endpoint = ENDPOINT_BASE + "pairingstatus/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("activationCode", activationCode);
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		JSONObject response = parseResponse();
		values.clear();
		values.put("pairingStatus", (PairingStatus)PairingStatus.valueOf((String)response.get("pairingStatus")));
	}
	
	@SuppressWarnings("unchecked")
	public void PairYubiKey(String otp) {

		this.name = "PairYubiKey";
		this.endpoint = ENDPOINT_BASE + "pairyubikey/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("otp", otp);
		reqBody.put("username", this.user.getUserName());
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		parseResponse();
		values.clear();
	}
	
	@SuppressWarnings("unchecked")
	public void StartOfflinePairing(OfflinePairingMethod pairingMethod) {

		this.name = "StartOfflinePairing";
		this.endpoint = ENDPOINT_BASE + "startofflinepairing/do";

		JSONObject reqBody = new JSONObject();
		
		if (pairingMethod == OfflinePairingMethod.SMS) {
			reqBody.put("type", OfflinePairingMethod.SMS.getValue());
			reqBody.put("pairingData", this.user.getPhoneNumber());
		} else if (pairingMethod == OfflinePairingMethod.VOICE) {
			reqBody.put("type", OfflinePairingMethod.VOICE.getValue());
			reqBody.put("pairingData", this.user.getPhoneNumber());
		} else if (pairingMethod == OfflinePairingMethod.EMAIL) {
			reqBody.put("type", OfflinePairingMethod.EMAIL.getValue());
			reqBody.put("pairingData", this.user.getEmail());
		}
		reqBody.put("username", this.user.getUserName());
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		JSONObject response = parseResponse();
		values.clear();
		this.lastSessionId = (String)response.get("sessionId");
	}
	
	@SuppressWarnings("unchecked")
	public void FinalizeOfflinePairing(String sessionId, String otp) {

		this.name = "FinalizeOfflinePairing";
		this.endpoint = ENDPOINT_BASE + "finalizeofflinepairing/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("otp", otp);
		reqBody.put("sessionId", sessionId);
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		parseResponse();
		values.clear();
	}
	
	@SuppressWarnings("unchecked")
	public void GetActivationCode() {

		this.name = "GetActivationCode";
		this.endpoint = ENDPOINT_BASE + "getactivationcode/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		JSONObject response = parseResponse();
		values.clear();
		this.lastActivationCode = (String)response.get("activationCode");
	}
	
    public void AuthenticateOnline(Application application, String authType) {
        AuthenticateOnline(application, authType, 0);
	}

	@SuppressWarnings("unchecked")
	public void AuthenticateOnline(Application application, String authType, long deviceId) {

		this.name = "AuthenticateOnline";
		this.endpoint = ENDPOINT_BASE + "authonline/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("authType", authType);
		reqBody.put("spAlias", application.getSpAlias());
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("clientData", this.clientData);
		
		if(deviceId > 0)
		    reqBody.put("deviceId", deviceId);
		
		JSONObject formParameters = new JSONObject();
		formParameters.put("sp_name", application.getName());
		if (application.getLogoUrl() != null || !application.getLogoUrl().isEmpty()) {
			formParameters.put("sp_logo", application.getLogoUrl());
		}

		reqBody.put("formParameters", formParameters);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		JSONObject response = parseResponse();

		if (this.wasSuccessful) {
			values.clear();
			this.lastSessionId = (String)response.get("sessionId");
		}
	}

	@SuppressWarnings("unchecked")
	public void AuthenticateOffline(String sessionId, String otp) {

		this.name = "AuthenticateOffline";
		this.endpoint = ENDPOINT_BASE + "authoffline/do";

		JSONObject reqBody = new JSONObject();
		reqBody.put("spAlias", "web");
		reqBody.put("otp", otp);
		reqBody.put("userName", this.user.getUserName());
		reqBody.put("sessionId", sessionId);
		reqBody.put("clientData", this.clientData);
		
		this.requestToken = buildRequestToken(reqBody);
		
		sendRequest();
		parseResponse();
		values.clear();
	}
	
	//private methods
	@SuppressWarnings("unchecked")
	private String buildRequestToken(JSONObject requestBody) {
		
		JSONObject requestHeader = buildRequestHeader();
		
		JSONObject payload = new JSONObject();
		payload.put("reqHeader", requestHeader);
		payload.put("reqBody", requestBody);
		
		JsonWebSignature jws = new JsonWebSignature();

		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		jws.setHeader("org_alias", this.orgAlias);
		jws.setHeader("token", this.token);
		
		jws.setPayload(payload.toJSONString());
		
	    // Set the verification key
	    HmacKey key = new HmacKey(Base64.decode(this.useBase64Key));
	    jws.setKey(key);
		
		String jwsCompactSerialization = null;
		try {
			jwsCompactSerialization = jws.getCompactSerialization();
		} catch (JoseException e) {
			e.printStackTrace();
		}
		
		this.requestToken = jwsCompactSerialization;
				
		return jwsCompactSerialization;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject buildRequestHeader() {
		
		JSONObject reqHeader = new JSONObject();
		reqHeader.put("locale", "en");
		reqHeader.put("orgAlias", this.orgAlias);
		reqHeader.put("secretKey", this.token);
		reqHeader.put("timestamp", getCurrentTimeStamp());
		reqHeader.put("version", this.apiVersion);
		
		return reqHeader;
	}
	
	static String getCurrentTimeStamp() {
		
		Date currentDate = new Date();
		SimpleDateFormat PingIDDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		PingIDDateFormat.setTimeZone(TimeZone.getTimeZone("America/Denver"));
		
		return PingIDDateFormat.format(currentDate);
	}
	
	private void sendRequest() {

		try {
			URL restUrl = new URL(this.getEndpoint());
			HttpURLConnection urlConnection = (HttpURLConnection)restUrl.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.addRequestProperty("Content-Type", "application/json");
			urlConnection.addRequestProperty("Accept", "*/*");

			urlConnection.setDoOutput(true);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
			outputStreamWriter.write(this.getRequestToken());
			outputStreamWriter.flush();
			outputStreamWriter.close();

			int responseCode = urlConnection.getResponseCode();
			this.responseCode = responseCode;

			if (responseCode == 200) {

				String encoding = urlConnection.getContentEncoding();
				InputStream is = urlConnection.getInputStream();
				String stringJWS = IOUtils.toString(is, encoding);
				this.responseToken = stringJWS;
				this.wasSuccessful = true;

				urlConnection.disconnect();
			} else {

				String encoding = urlConnection.getContentEncoding();
				InputStream is = urlConnection.getErrorStream();
				String stringJWS = IOUtils.toString(is, encoding);
				this.responseToken = stringJWS;
				this.wasSuccessful = false;

				urlConnection.disconnect();
			}
		} catch (Exception ex) {
			this.responseCode = 500;
			this.wasSuccessful = false;
		}
	}
	
	private JSONObject parseResponse() {

		JSONParser parser = new JSONParser();
		JSONObject responsePayloadJSON = null;

		try {

			JsonWebSignature responseJWS = new JsonWebSignature();
			responseJWS.setCompactSerialization(this.responseToken);
			HmacKey key = new HmacKey(Base64.decode(this.useBase64Key));
			responseJWS.setKey(key);
			responsePayloadJSON = (JSONObject)parser.parse(responseJWS.getPayload());

			// workaround for PingID API 4.5 beta
			if (responsePayloadJSON.containsKey("responseBody")) {
				responsePayloadJSON = (JSONObject)responsePayloadJSON.get("responseBody");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (responsePayloadJSON != null) {
			this.errorId = (long)responsePayloadJSON.get("errorId");
			this.errorMsg = (String)responsePayloadJSON.get("errorMsg");
			this.uniqueMsgId = (String)responsePayloadJSON.get("uniqueMsgId");
			this.clientData = (String)responsePayloadJSON.get("clientData");
		} else {
			this.errorId = 501;
			this.errorMsg = "Could not parse JWS";
			this.uniqueMsgId = "";
			this.clientData = "";
			this.wasSuccessful = false;
		}

		return responsePayloadJSON;
	}
}
