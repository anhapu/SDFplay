package models.enums;

import com.avaje.ebean.annotation.EnumMapping;

@EnumMapping(nameValuePairs="INIT=0, REFUSE=1, RESPONSE=2, FINAL_REFUSE=3, APPROVE=4, INVALID=5")
public enum States {
	INIT, REFUSE, RESPONSE, FINAL_REFUSE, APPROVE, INVALID
}
