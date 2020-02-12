package tech.mistermel.domus.web;

import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import tech.mistermel.domus.Domus;
import tech.mistermel.domus.device.Device;
import tech.mistermel.domus.device.DeviceType;
import tech.mistermel.domus.web.WebServer.Route;

public class CreateDeviceRoute implements Route {

	@Override
	public Response serve(IHTTPSession session, JSONObject json) {
		if(session.getMethod() == Method.OPTIONS) {
			Response response = WebServer.newFixedLengthResponse(Response.Status.NO_CONTENT, null, null);
			response.addHeader("Access-Control-Allow-Methods", "POST");
			response.addHeader("Access-Control-Allow-Headers", "Content-Type");
			return response;
		}
		
		if(session.getMethod() != Method.POST)
			return WebServer.response(Response.Status.METHOD_NOT_ALLOWED);
		
		if(json == null)
			return WebServer.response(Response.Status.BAD_REQUEST);
		
		String name = json.optString("name");
		if(name.isEmpty())
			return WebServer.response(Response.Status.BAD_REQUEST, "Enter a name");
		
		String ip = json.optString("ip");
		if(ip.isEmpty())
			return WebServer.response(Response.Status.BAD_REQUEST, "Enter an IP");
		if(!ip.matches("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$"))
			return WebServer.response(Response.Status.BAD_REQUEST, "Invalid IP address");
		
		String typeStr = json.optString("type");
		if(typeStr.isEmpty())
			return WebServer.response(Response.Status.BAD_REQUEST, "Select a device type");
		DeviceType type = DeviceType.lookup(typeStr);
		if(type == null)
			return WebServer.response(Response.Status.BAD_REQUEST, "Select a device type");
			
		Device device = new Device(name, ip, type);
		Domus.getInstance().getDeviceManager().addDevice(device);
		
		JSONObject res = WebServer.json(true, null);
		res.put("device", device.toJson());
		return WebServer.response(Response.Status.OK, res);
	}
	
	@Override
	public boolean isAuthRequired() {
		return true;
	}
	
}
