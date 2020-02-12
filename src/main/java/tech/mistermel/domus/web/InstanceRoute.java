package tech.mistermel.domus.web;

import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import tech.mistermel.domus.Domus;
import tech.mistermel.domus.web.WebServer.Route;

public class InstanceRoute implements Route {

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
		res.put("name", Domus.getInstance().getInstanceName());
		res.put("sunrise", Domus.getInstance().getTimeManager().getSunrise());
		res.put("sunset", Domus.getInstance().getTimeManager().getSunset());
		return WebServer.response(Response.Status.OK, res);
	}

	@Override
	public boolean isAuthRequired() {
		return true;
	}
	
}
