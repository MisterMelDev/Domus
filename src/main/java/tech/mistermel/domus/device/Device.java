package tech.mistermel.domus.device;

import java.util.Properties;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.mistermel.domus.Domus;

public class Device {
	
	private static Logger logger = LoggerFactory.getLogger(Device.class);
	
	private String id;
	private String name;
	private String ip;
	
	private Automation automation;
	private DeviceType type;
	
	private boolean enabled;
	private boolean connected;
	
	public Device(String name, String ip, DeviceType type) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.ip = ip;
		this.automation = Automation.NONE;
		this.type = type;
		
		this.enabled = type.getImplementation().isEnabled(this);
		logger.debug("Device {} ({}) created, enabled: {}", name, id, enabled);
	}
	
	public Device(Properties props) {
		this.id = props.getProperty("id");
		this.name = props.getProperty("name");
		this.ip = props.getProperty("ip");
		this.automation = Automation.valueOf(props.getProperty("automation"));
		this.type = DeviceType.valueOf(props.getProperty("type"));
		
		this.enabled = type.getImplementation().isEnabled(this);
		logger.debug("Device {} ({}) loaded, enabled: {}", name, id, enabled);
	}
	
	public void save(Properties props) {
		props.put("id", id);
		props.put("name", name);
		props.put("ip", ip);
		props.put("automation", automation.name());
		props.put("type", type.name());
	}
	
	public void setEnabled(boolean enabled) {
		boolean success;
		if(enabled) {
			success = type.getImplementation().turnOn(this);
		} else {
			success = type.getImplementation().turnOff(this);
		}
		
		if(success)
			this.enabled = enabled;
	}
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void setAutomation(Automation automation) {
		this.automation = automation;
		
		boolean isNight = Domus.getInstance().getTimeManager().isNight();
		if(automation == Automation.DURING_DAY) {
			this.setEnabled(!isNight);
		} else if(automation == Automation.DURING_NIGHT) {
			this.setEnabled(isNight);
		}
	}
	
	public void setDeviceType(DeviceType type) {
		this.type = type;
		this.enabled = type.getImplementation().isEnabled(this);
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIp() {
		return ip;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public Automation getAutomation() {
		return automation;
	}
	
	public DeviceType getType() {
		return type;
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("name", name);
		json.put("ip", ip);
		json.put("automation", automation.toJson());
		json.put("type", type.toJson());
		json.put("enabled", enabled);
		json.put("connected", connected);
		return json;
	}
	
	public enum Automation {
		NONE("None"), DURING_DAY("On during the day"), DURING_NIGHT("On during the night");
		
		private String text;
		
		private Automation(String text) {
			this.text = text;
		}
		
		public String getText() {
			return text;
		}
		
		public JSONObject toJson() {
			JSONObject json = new JSONObject();
			json.put("id", this.name());
			json.put("text", this.getText());
			return json;
		}
		
		public static JSONArray toJsonArray() {
			JSONArray json = new JSONArray();
			for(Automation automation : Automation.values()) {
				json.put(automation.toJson());
			}
			return json;
		}
		
		public static Automation lookup(String name) {
			for(Automation automation : values()) {
				if(automation.name().equals(name)) {
					return automation;
				}
			}
			return null;
		}
		
	}
	
}
