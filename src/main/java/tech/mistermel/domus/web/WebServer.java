package tech.mistermel.domus.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import tech.mistermel.domus.Domus;

public class WebServer extends NanoHTTPD {

	private static Logger logger = LoggerFactory.getLogger(WebServer.class);
	
	private Map<String, Route> routes = new HashMap<>();
	private Set<String> tokens = new HashSet<>();
	
	public WebServer(int port) {
		super(port);
	}
	
	public void registerRoute(String uri, Route route) {
		routes.put(uri, route);
		logger.debug("Route {} registered, auth required: {}", uri, route.isAuthRequired());
	}
	
	public String generateToken() {
		String token = UUID.randomUUID().toString();
		tokens.add(token);
		return token;
	}
	
	public boolean removeToken(String token) {
		return tokens.remove(token);
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		// GET params are already removed from the uri
		Route route = routes.get(session.getUri());
		
		if(route == null)
			return response(Response.Status.NOT_FOUND);
		
		if(route.isAuthRequired() && !this.isAuthenticated(session))
			return response(Response.Status.FORBIDDEN);
		
		JSONObject json = null;
		if(session.getMethod() == Method.POST && "application/json".equals(session.getHeaders().get("content-type"))) {
			try {
				Map<String, String> map = new HashMap<>();
				session.parseBody(map);
				
				if(map.containsKey("postData")) {
					json = new JSONObject(map.get("postData"));
				}
			} catch (IOException | ResponseException e) {
				logger.error("Error occurred while attempting to parse body", e);
			} catch	(JSONException e) {
				return response(Response.Status.BAD_REQUEST, "Malformed JSON");
			}
		}
		
		Response response = route.serve(session, json);
		response.addHeader("Access-Control-Allow-Headers", "x-domus-token, content-type");
		return response;
	}
	
	public boolean isAuthenticated(IHTTPSession session) {
		if(!Domus.getInstance().isAuthEnabled() || session.getMethod() == Method.OPTIONS)
			return true;
		
		String token = session.getHeaders().get("x-domus-token");
		if(token == null)
			return false;
		
		return tokens.contains(token);
	}
	
	public interface Route {

		public Response serve(IHTTPSession session, JSONObject json);
		public boolean isAuthRequired();
		
	}
	
	public static JSONObject json(boolean success, String msg) {
		JSONObject res = new JSONObject();
		res.put("success", success);
		res.put("msg", msg);
		return res;
	}
	
	public static Response response(Status status, JSONObject response) {
		return  newFixedLengthResponse(status, "application/json", response.toString());
	}
	
	public static Response response(Status status) {
		return response(status, status.getDescription().substring(4));
	}
	
	public static Response response(Status status, String msg) {
		return newFixedLengthResponse(status, "application/json", json(status.getRequestStatus() / 100 == 2, msg).toString());
	}

}
