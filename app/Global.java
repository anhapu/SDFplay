import java.util.List;
import java.util.Map;

import com.avaje.ebean.Ebean;

import models.User;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Yaml;

public class Global extends GlobalSettings {

	@Override
	public void onStart(Application app) {
		Logger.info("Application has started");
		if(User.find.findRowCount() == 0) {
			Logger.info("Filling the database with initial data (conf/initial-data.yml)");
			@SuppressWarnings("unchecked")
			Map<String,List<Object>> all = (Map<String,List<Object>>)Yaml.load("initial-data.yml");
			Ebean.save(all.get("users"));
		}
	}
	
}
