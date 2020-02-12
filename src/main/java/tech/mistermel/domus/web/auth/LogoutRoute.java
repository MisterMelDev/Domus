package tech.mistermel.domus.web.auth;

import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import tech.mistermel.domus.Domus;
import tech.mistermel.domus.web.WebServer;
import tech.mistermel.domus.web.WebServer.Route;

public class LogoutRoute implements Route {

	@Override
	public Response serve(IHTTPSession session, JSONObject json) {
		if(session.getMethod() == Method.OPTIONS) {
			Response response = WebServer.newFixedLengthResponse(Response.Status.NO_CONTENT, null, null);
			response.addHeader("Access-Control-Allow-Methods", "POST");
			return response;
		}
		
		if(session.getMethod() != Method.POST)
			return WebServer.response(Response.Status.METHOD_NOT_ALLOWED);
		
		String token = session.getHeaders().get("x-domus-token");
		Domus.getInstance().getWebServer().removeToken(token);
		
		return WebServer.response(Response.Status.OK);
	}
	
	@Override
	public boolean isAuthRequired() {
		return true;
	}
	
}
