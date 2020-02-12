package tech.mistermel.domus.web.auth;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import tech.mistermel.domus.Domus;
import tech.mistermel.domus.web.WebServer;
import tech.mistermel.domus.web.WebServer.Route;

public class LoginRoute implements Route {

	private static Logger logger = LoggerFactory.getLogger(LoginRoute.class);
	
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
		
		String username = json.optString("username");
		String password = json.optString("password");
		
		if(username.isEmpty() || password.isEmpty())
			return WebServer.response(Response.Status.FORBIDDEN, "Username and password must be entered");
		
		if(!username.equals(Domus.getInstance().getUsername()) || !password.equals(Domus.getInstance().getPassword()))
			return WebServer.response(Response.Status.FORBIDDEN, "Username and/or password is incorrect");
		
		logger.info("Logged in from {}", session.getRemoteIpAddress());
		
		JSONObject res = WebServer.json(true, null);
		String token = Domus.getInstance().getWebServer().generateToken();
		res.put("token", token);
		return WebServer.response(Response.Status.OK, res);
	}
	
	@Override
	public boolean isAuthRequired() {
		return false;
	}
	
}
