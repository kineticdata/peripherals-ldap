package com.kineticdata.bridgehub.adapter.ldap;

import com.kineticdata.bridgehub.adapter.QualificationParser;

/**
 *
 */
public class LdapQualificationParser extends QualificationParser {
    public String encodeParameter(String name, String value) {
        return value;
    }
}
