package tech.mistermel.domus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;
import tech.mistermel.domus.device.DeviceManager;
import tech.mistermel.domus.web.AutomationsRoute;
import tech.mistermel.domus.web.CreateDeviceRoute;
import tech.mistermel.domus.web.DeviceRoute;
import tech.mistermel.domus.web.DeviceTypesRoute;
import tech.mistermel.domus.web.HomeRoute;
import tech.mistermel.domus.web.InstanceRoute;
import tech.mistermel.domus.web.WebServer;
import tech.mistermel.domus.web.auth.LoginRoute;
import tech.mistermel.domus.web.auth.LogoutRoute;

public class Domus {

	private static Domus instance;
	private static Logger logger = LoggerFactory.getLogger(WebServer.class);
	
	private WebServer webServer;
	private DeviceManager deviceManager;
	private TimeManager timeManager;
	
	private boolean authEnabled = true;
	private String username;
	private String password;
	
	private String deviceToken;
	private String instanceName;
	
	private Domus(int port, String username, String password, String deviceToken, String instanceName, double longitude, double latitude) {
		this.webServer = new WebServer(port);
		this.deviceManager = new DeviceManager();
		this.timeManager = new TimeManager(longitude, latitude);
		
		this.deviceToken = deviceToken;
		this.instanceName = instanceName;
		this.username = username;
		this.password = password;
		
		if(username == null || password == null) {
			logger.warn("Username and/or password not specified, authentication will be disabled.");
			authEnabled = false;
		}
		
		webServer.registerRoute("/", new HomeRoute());
		webServer.registerRoute("/instance", new InstanceRoute());
		webServer.registerRoute("/device", new DeviceRoute());
		webServer.registerRoute("/device/create", new CreateDeviceRoute());
		webServer.registerRoute("/device/types", new DeviceTypesRoute());
		webServer.registerRoute("/device/automations", new AutomationsRoute());
		
		if(authEnabled) {
			webServer.registerRoute("/auth/login", new LoginRoute());
			webServer.registerRoute("/auth/logout", new LogoutRoute());
		}
	}
	
	public void start() {
		try {
			deviceManager.load();
			timeManager.schedule(false);
			
			webServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
			logger.info("Webserver started on port {}", webServer.getListeningPort());
		} catch (IOException e) {
			logger.error("Failed to start webserver", e);
		}
	}
	
	public WebServer getWebServer() {
		return webServer;
	}
	
	public DeviceManager getDeviceManager() {
		return deviceManager;
	}
	
	public TimeManager getTimeManager() {
		return timeManager;
	}
	
	public boolean isAuthEnabled() {
		return authEnabled;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getDeviceToken() {
		return deviceToken;
	}
	
	public String getInstanceName() {
		return instanceName;
	}
	
	public static void main(String[] args) {
		File configFile = new File("configuration");
		if(!configFile.exists()) {
			try {
				configFile.createNewFile();
				logger.warn("No configuration file present, file has been created. Exiting.");
			} catch (IOException e) {
				logger.error("Error while attempting to create configuration file. Exiting.", e);
			}
			
			return;
		}
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configFile));
		} catch (IOException e) {
			logger.error("Error while loading configuration file. Exiting.", e);
		}
		
		try {
			int port = Integer.parseInt(properties.getProperty("port"));
			String username = properties.getProperty("web.username");
			String password = properties.getProperty("web.password");
			String deviceToken = properties.getProperty("device.token");
			String instanceName = properties.getProperty("web.name");
			
			double longitude = Double.parseDouble(properties.getProperty("location.longitude"));
			double latitude = Double.parseDouble(properties.getProperty("location.latitude"));
			
			instance = new Domus(port, username, password, deviceToken, instanceName, longitude, latitude);
			instance.start();
		} catch (NumberFormatException e) {
			logger.warn("Configuration file is malformed. Exiting.", e);
		}
	}
	
	public static Domus getInstance() {
		return instance;
	}
	
}
