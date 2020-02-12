package tech.mistermel.domus;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import tech.mistermel.domus.device.Device;
import tech.mistermel.domus.device.Device.Automation;

public class TimeManager {

	private static Logger logger = LoggerFactory.getLogger(TimeManager.class);
	
	private SunriseSunsetCalculator calculator;
	private Timer timer;
	
	private boolean isNight;
	private long sunrise;
	private long sunset;
	
	public TimeManager(double longitude, double latitude) {
		Location location = new Location(longitude, latitude);
		this.calculator = new SunriseSunsetCalculator(location, "Europe/Amsterdam");
		this.timer = new Timer("SunTimer");
	}
	
	private void onSunrise() {
		this.isNight = false;
		logger.info("The night has ended");
		
		for(Device device : Domus.getInstance().getDeviceManager().getDevices()) {
			if(device.getAutomation() == Automation.DURING_DAY)
				device.setEnabled(true);
			else if(device.getAutomation() == Automation.DURING_NIGHT)
				device.setEnabled(false);
		}
	}
	
	private void onSunset(boolean reschedule) {
		this.isNight = true;
		logger.info("The night has started");
		
		if(reschedule)
			schedule(true);
		
		for(Device device : Domus.getInstance().getDeviceManager().getDevices()) {
			if(device.getAutomation() == Automation.DURING_NIGHT)
				device.setEnabled(true);
			else if(device.getAutomation() == Automation.DURING_DAY)
				device.setEnabled(false);
		}
	}
	
	public void schedule(boolean offset) {
		Calendar date = Calendar.getInstance();
		if(offset)
			date.add(Calendar.HOUR_OF_DAY, 24);
		logger.debug("Calculating for {}-{} (offset: {})", date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH), offset);
		
		Calendar now = Calendar.getInstance();
		
		Calendar sunset = calculator.getCivilSunsetCalendarForDate(date);
		if(now.after(sunset)) {
			logger.debug("Sunset has already passed (was at {}:{}), scheduling tasks for tommorow", sunset.get(Calendar.HOUR_OF_DAY), sunset.get(Calendar.MINUTE));
			onSunset(false);
			
			schedule(true);
			return;
		}
		
		Calendar sunrise = calculator.getCivilSunriseCalendarForDate(date);
		logger.info("Calculated day: {}:{}-{}:{}", sunrise.get(Calendar.HOUR_OF_DAY), sunrise.get(Calendar.MINUTE), sunset.get(Calendar.HOUR_OF_DAY), sunset.get(Calendar.MINUTE));
		
		this.sunrise = sunrise.getTimeInMillis();
		this.sunset = sunset.getTimeInMillis();
		
		TimerTask sunriseTask = new TimerTask() {
			public void run() {
				onSunrise();
			}
		};
		timer.schedule(sunriseTask, sunrise.getTime());
		
		TimerTask sunsetTask = new TimerTask() {
			public void run() {
				onSunset(true);
			}
		};
		timer.schedule(sunsetTask, sunset.getTime());
	}
	
	public boolean isNight() {
		return isNight;
	}
	
	public long getSunrise() {
		return sunrise;
	}
	
	public long getSunset() {
		return sunset;
	}
	
}
