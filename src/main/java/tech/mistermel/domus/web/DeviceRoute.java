package tech.mistermel.domus.web;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import tech.mistermel.domus.Domus;
import tech.mistermel.domus.device.Device;
import tech.mistermel.domus.device.Device.Automation;
import tech.mistermel.domus.device.DeviceType;
import tech.mistermel.domus.web.WebServer.Route;

public class DeviceRoute implements Route {

	@Override
	public Response serve(IHTTPSession session, JSONObject json) {
		if(session.getMethod() == Method.OPTIONS) {
			Response response = WebServer.newFixedLengthResponse(Response.Status.NO_CONTENT, null, null);
			response.addHeader("Access-Control-Allow-Methods", "GET, POST");
			response.addHeader("Access-Control-Allow-Headers", "Content-Type");
			return response;
		}
		
		if(session.getMethod() == Method.GET)
			return get();
		
		if(session.getMethod() == Method.POST)
			return post(json);
			
		return WebServer.response(Response.Status.METHOD_NOT_ALLOWED);
	}
	
	private Response get() {
		JSONObject res = WebServer.json(true, null);
		JSONArray deviceArray = new JSONArray();
		res.put("devices", deviceArray);
		
		for(Device device : Domus.getInstance().getDeviceManager().getDevices()) {
			deviceArray.put(device.toJson());
		}
		
		return WebServer.response(Response.Status.OK, res);
	}
	
	private Response post(JSONObject json) {
		if(json == null)
			return WebServer.response(Response.Status.BAD_REQUEST);
		
		String id = json.optString("id");
		if(id.isEmpty())
			return WebServer.response(Response.Status.BAD_REQUEST, "Specify device ID");
		
		Device device = Domus.getInstance().getDeviceManager().getDevice(id);
		if(device == null)
			return WebServer.response(Response.Status.NOT_FOUND, "Device not found");

		if(json.optBoolean("remove")) {
			Domus.getInstance().getDeviceManager().remove(device);
			return WebServer.response(Response.Status.OK);
		}
		
		boolean hasChanged = false;
		boolean doSave = false;
		
		String name = json.optString("name");
		if(!name.isEmpty()) {
			device.setName(name);
			hasChanged = true;
			doSave = true;
		}
		
		String ip = json.optString("ip");
		if(!ip.isEmpty()) {
			device.setIp(ip);
			hasChanged = true;
			doSave = true;
		}
		
		String automationStr = json.optString("automation");
		if(!automationStr.isEmpty()) {
			Automation automation = Automation.lookup(automationStr);
			if(automation == null)
				return WebServer.response(Response.Status.BAD_REQUEST, "Invalid automation type");
			
			device.setAutomation(automation);
			hasChanged = true;
			doSave = true;
		}
		
		String deviceTypeStr = json.optString("type");
		if(!deviceTypeStr.isEmpty()) {
			DeviceType deviceType = DeviceType.lookup(deviceTypeStr);
			if(deviceType == null)
				return WebServer.response(Response.Status.BAD_REQUEST, "Invalid device type");
			
			device.setDeviceType(deviceType);
			hasChanged = true;
			doSave = true;
		}
		
		boolean enabled = json.optBoolean("enabled");
		if(json.has("enabled")) {
			device.setEnabled(enabled);
			hasChanged = true;
		}
		
		if(hasChanged) {
			if(doSave)
				Domus.getInstance().getDeviceManager().save(device);
			
			JSONObject res = WebServer.json(true, null);
			res.put("device", device.toJson());
			return WebServer.response(Response.Status.OK, res);
		}
		
		return WebServer.response(Response.Status.BAD_REQUEST, "Must specify at least one change");
	}
	
	@Override
	public boolean isAuthRequired() {
		return true;
	}
	
}
