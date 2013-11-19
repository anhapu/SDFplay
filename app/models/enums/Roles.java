package models.enums;

import com.avaje.ebean.annotation.EnumMapping;


@EnumMapping(nameValuePairs="ADMIN=0, USER=1")
public enum Roles
{
    ADMIN,
    USER
}
