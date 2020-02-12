package tech.mistermel.domus.device;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tech.mistermel.domus.Domus;

public enum DeviceType {
	DOMUS("Domus", new DomusDevice()), TASMOTA("Tasmota (Sonoff)", new TasmotaDevice());
	
	private String text;
	private IDeviceType implementation;
	
	private DeviceType(String text, IDeviceType implementation) {
		this.text = text;
		this.implementation = implementation;
	}
	
	public String getText() {
		return text;
	}
	
	public IDeviceType getImplementation() {
		return implementation;
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("id", this.name());
		json.put("text", this.getText());
		return json;
	}
	
	public static JSONArray toJsonArray() {
		JSONArray json = new JSONArray();
		for(DeviceType type : DeviceType.values()) {
			json.put(type.toJson());
		}
		return json;
	}
	
	public static DeviceType lookup(String name) {
		for(DeviceType type : values()) {
			if(type.name().equals(name)) {
				return type;
			}
		}
		return null;
	}
	
	public static interface IDeviceType {
		
		/* Should return true if the device has turned on, false if the operation failed */
		public boolean turnOn(Device device);
		
		/* Should return true if the device has turned off, false if the operation failed */
		public boolean turnOff(Device device);
		
		/* Should return true if the device is enabled, false if it is disabled */
		public boolean isEnabled(Device device);
	}
	
	public static class DomusDevice implements IDeviceType {
		
		private static Logger logger = LoggerFactory.getLogger(DomusDevice.class);
		
		private OkHttpClient httpClient;
		
		public DomusDevice() {
			this.httpClient = new OkHttpClient();
		}

		@Override
		public boolean turnOn(Device device) {
			String url = "http://" + device.getIp() + "/on?token=" + Domus.getInstance().getDeviceToken();
			String response = getResponse(url);
			
			boolean success = response != null;
			device.setConnected(success);

			return success;
		}

		@Override
		public boolean turnOff(Device device) {
			String url = "http://" + device.getIp() + "/off?token=" + Domus.getInstance().getDeviceToken();
			String response = getResponse(url);
			
			boolean success = response != null;
			device.setConnected(success);
			
			return success;
		}

		@Override
		public boolean isEnabled(Device device) {
			String url = "http://" + device.getIp() + "/state?token=" + Domus.getInstance().getDeviceToken();
			String response = getResponse(url);
			
			if(response == null) {
				device.setConnected(false);
				return false;
			}
			
			device.setConnected(true);
			return response.equals("on");
		}
		
		private String getResponse(String url) {
			Request request = new Request.Builder()
					.url(url)
					.build();
			
			try(Response response = httpClient.newCall(request).execute()) {
				return response.body().string();
			} catch(IOException e) {
				logger.error("Error while sending HTTP request to {}", url, e);
			}
			
			return null;
		}
		
	}
	
	public static class TasmotaDevice implements IDeviceType {
		
		private static Logger logger = LoggerFactory.getLogger(TasmotaDevice.class);
		
		private OkHttpClient httpClient;
		
		public TasmotaDevice() {
			this.httpClient = new OkHttpClient();
		}

		@Override
		public boolean turnOn(Device device) {
			String url = "http://" + device.getIp() + "/cm?cmnd=power%20on";
			JSONObject response = getJsonResponse(url);
			
			if(response == null)
				return false;
			
			String power = response.optString("POWER");
			return power != null && power.equals("ON");
		}

		@Override
		public boolean turnOff(Device device) {
			String url = "http://" + device.getIp() + "/cm?cmnd=power%20off";
			JSONObject response = getJsonResponse(url);
			
			if(response == null)
				return false;
			
			String power = response.optString("POWER");
			return power != null && power.equals("OFF");
		}

		@Override
		public boolean isEnabled(Device device) {
			String url = "http://" + device.getIp() + "/cm?cmnd=status";
			JSONObject response = getJsonResponse(url);
			
			if(response == null) {
				device.setConnected(false);
				return false;
			}
			
			JSONObject statusObj = response.optJSONObject("Status");
			if(statusObj == null) {
				logger.warn("Device {} returned an invalid response", device.getName());
				device.setConnected(false);
				return false;
			}
			
			device.setConnected(true);
			int power = response.optInt("Power");
			return power == 1;
		}
		
		private JSONObject getJsonResponse(String url) {
			Request request = new Request.Builder()
					.url(url)
					.build();
			
			logger.debug("Requesting {}", url);
			
			try(Response response = httpClient.newCall(request).execute()) {
				return new JSONObject(response.body().string());
			} catch(IOException e) {
				logger.error("Error while sending HTTP request to {}", url, e);
			} catch(JSONException e) {
				logger.error("Invalid JSON returned while sending HTTP request to {}", url, e);
			}
			
			return null;
		}
		
	}
	
}
