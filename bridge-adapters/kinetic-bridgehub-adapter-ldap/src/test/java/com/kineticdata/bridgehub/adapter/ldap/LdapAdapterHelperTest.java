package com.kineticdata.bridgehub.adapter.ldap;

import com.kineticdata.bridgehub.adapter.BridgeError;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LdapAdapterHelperTest {

    @Test
    public void test_buildFilter_emptyQuery() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "";
        Map parameters = new HashMap();
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(objectClass=user)", filter);
    }

    @Test
    public void test_buildFilter_noParams() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "(samaccountname=userid)";
        Map parameters = new HashMap();
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(&(objectClass=user)(samaccountname=userid))", filter);
    }

    @Test
    public void test_buildFilter_withParams() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "(samaccountname=<%=parameter[\"samaccountname\"]%>)";
        Map parameters = new HashMap();
        parameters.put("samaccountname", "userid");
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(&(objectClass=user)(samaccountname=userid))", filter);
    }

    @Test
    public void test_buildFilter_withParensInParams() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "<%=parameter[\"a_query\"]%>";
        Map parameters = new HashMap();
        parameters.put("a_query", "(samaccountname=userid)");
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(&(objectClass=user)(samaccountname=userid))", filter);
    }

    @Test
    public void test_buildFilter_withDoubleParams() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "(samaccountname=<%=parameter[\"samaccountname\"]%>)(email=<%=parameter[\"email\"]%>)";
        Map parameters = new HashMap();
        parameters.put("samaccountname", "userid");
        parameters.put("email", "someemail@somewhere.com");
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(&(objectClass=user)(samaccountname=userid)(email=someemail@somewhere.com))", filter);
    }

    @Test
    public void test_buildFilter_withDoubleParamsOr() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "(|(samaccountname=<%=parameter[\"samaccountname\"]%>)(email=<%=parameter[\"email\"]%>))";
        Map parameters = new HashMap();
        parameters.put("samaccountname", "userid");
        parameters.put("email", "someemail@somewhere.com");
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(&(objectClass=user)(|(samaccountname=userid)(email=someemail@somewhere.com)))", filter);
    }

    @Test
    public void test_buildFilter_noParams_noParens() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "samaccountname=userid";
        Map parameters = new HashMap();
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(objectClass=user)", filter);
    }

    @Test
    public void test_buildFilter_withParams_noParens() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "samaccountname=<%=parameter[\"samaccountname\"]%>";
        Map parameters = new HashMap();
        parameters.put("samaccountname", "userid");
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(objectClass=user)", filter);
    }

    @Test
    public void test_buildFilter_withAddedSearchBase() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "OU=Users(samaccountname=userid)";
        Map parameters = new HashMap();
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(&(objectClass=user)(samaccountname=userid))", filter);
    }

    @Test
    public void test_buildFilter_withAddedSearchBase_WithParams() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "<%=parameter[\"Added Search Base\"]%>(samaccountname=<%=parameter[\"samaccountname\"]%>)";
        Map parameters = new HashMap();
        parameters.put("Added Search Base", "OU=Users");
        parameters.put("samaccountname", "userid");
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(&(objectClass=user)(samaccountname=userid))", filter);
    }

    @Test
    public void test_buildFilter_ComplexFilter() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "(|(distinguishedName=OU=Users,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users2,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users3,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users4,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users5,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users6,OU=ACME,DC=ACME,DC=COM))";
        Map parameters = new HashMap();
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("(&(objectClass=user)(|(distinguishedName=OU=Users,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users2,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users3,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users4,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users5,OU=ACME,DC=ACME,DC=COM)(distinguishedName=OU=Users6,OU=ACME,DC=ACME,DC=COM)))", filter);
    }

    @Test
    public void test_buildSearchBase_emptyQuery() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "";
        Map parameters = new HashMap();
        String bridgeSearchBase = "OU=corp,DC=acme,DC=com";

        String finalSearchBase = null;
        try {
            finalSearchBase = adapter.buildSearchBase(query, parameters, bridgeSearchBase);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("OU=corp,DC=acme,DC=com", finalSearchBase);
    }

    @Test
    public void test_buildSearchBase_noParams() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "OU=Users(samaccountname=userid)";
        Map parameters = new HashMap();
        String bridgeSearchBase = "OU=corp,DC=acme,DC=com";

        String finalSearchBase = null;
        try {
            finalSearchBase = adapter.buildSearchBase(query, parameters, bridgeSearchBase);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("OU=Users,OU=corp,DC=acme,DC=com", finalSearchBase);
    }

    @Test
    public void test_buildSearchBase_noParams_splitBases() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "OU=SubUsers(samaccountname=userid),OU=Users";
        Map parameters = new HashMap();
        String bridgeSearchBase = "OU=corp,DC=acme,DC=com";

        String finalSearchBase = null;
        try {
            finalSearchBase = adapter.buildSearchBase(query, parameters, bridgeSearchBase);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("OU=SubUsers,OU=Users,OU=corp,DC=acme,DC=com", finalSearchBase);
    }

    @Test
    public void test_buildSearchBase_noAdditions() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "(samaccountname=userid)";
        Map parameters = new HashMap();
        String bridgeSearchBase = "OU=corp,DC=acme,DC=com";

        String finalSearchBase = null;
        try {
            finalSearchBase = adapter.buildSearchBase(query, parameters, bridgeSearchBase);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("OU=corp,DC=acme,DC=com", finalSearchBase);
    }

    @Test
    public void test_buildSearchBase_noParensInQuery() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "samaccountname=userid";
        Map parameters = new HashMap();
        String bridgeSearchBase = "OU=corp,DC=acme,DC=com";

        String finalSearchBase = null;
        try {
            finalSearchBase = adapter.buildSearchBase(query, parameters, bridgeSearchBase);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("samaccountname=userid,OU=corp,DC=acme,DC=com", finalSearchBase);
    }

    @Test
    public void test_buildSearchBase_usingParam() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "<%=parameter[\"Added Search Base\"]%>(samaccountname=userid)";
        Map parameters = new HashMap();
        parameters.put("Added Search Base", "OU=Users");
        String bridgeSearchBase = "OU=corp,DC=acme,DC=com";

        String finalSearchBase = null;
        try {
            finalSearchBase = adapter.buildSearchBase(query, parameters, bridgeSearchBase);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals("OU=Users,OU=corp,DC=acme,DC=com", finalSearchBase);
    }
}
