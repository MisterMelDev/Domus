package tech.mistermel.domus.web;

import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import tech.mistermel.domus.device.Device;
import tech.mistermel.domus.web.WebServer.Route;

public class AutomationsRoute implements Route {

	@Override
	public Response serve(IHTTPSession session, JSONObject json) {
		if(session.getMethod() == Method.OPTIONS) {
			Response response = WebServer.newFixedLengthResponse(Response.Status.NO_CONTENT, null, null);
			response.addHeader("Access-Control-Allow-Methods", "GET");
			return response;
		}
		
		if(session.getMethod() != Method.GET)
			return WebServer.response(Response.Status.METHOD_NOT_ALLOWED);
		
		JSONObject res = WebServer.json(true, null);
		res.put("automations", Device.Automation.toJsonArray());
		return WebServer.response(Response.Status.OK, res);
	}

	@Override
	public boolean isAuthRequired() {
		return true;
	}
	
}
