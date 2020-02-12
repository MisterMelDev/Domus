package tech.mistermel.domus.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManager {

	private static Logger logger = LoggerFactory.getLogger(DeviceManager.class);

	private Map<String, Device> devices = new HashMap<>();

	public void load() {
		File folder = new File("devices");
		if(!folder.isDirectory())
			folder.mkdirs();

		for(File file : folder.listFiles()) {
			if(file.isDirectory())
				continue;

			try {
				Properties properties = new Properties();
				properties.load(new FileInputStream(file));

				Device device = new Device(properties);
				devices.put(device.getId(), device);

				logger.info("Loaded device {} (ip: {})", device.getName(), device.getIp());
			} catch (IOException e) {
				logger.error("Error while loading file devices/{}", file.getName(), e);
			}
		}

		logger.info("Loaded {} device(s)", devices.size());
	}

	public void save(Device device) {
		try {
			File file = new File("devices/" + device.getId());
			if(!file.exists())
				file.createNewFile();

			Properties properties = new Properties();
			device.save(properties);
			properties.store(new FileOutputStream(file), null);
		} catch (IOException e) {
			logger.error("Error while saving file devices/{}", device.getId(), e);
		}
	}
	
	public void addDevice(Device device) {
		devices.put(device.getId(), device);
		save(device);
	}
	
	public Device getDevice(String id) {
		return devices.get(id);
	}
	
	public void remove(Device device) {
		devices.remove(device.getId());
		new File("devices/" + device.getId()).delete();
	}

	public Collection<Device> getDevices() {
		return devices.values();
	}

}
