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
    public void test_buildFilter_ComplexFilter_withParams() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "(&" +
            "(|(employeeNumber=<%=parameter[\"Military ID\"]%>))" +
            "(|(givenName=<%=parameter[\"First Name\"]%>))" +
            "(|(sn=<%=parameter[\"Last Name\"]%>))" +
            "(|(departmentNumber=<%=parameter[\"Installation\"]%>))" +
            "(|(title=<%=parameter[\"Rank\"]%>))" +
            "(|(employeeType=<%=parameter[\"VIP\"]%>))" +
            "(|(mail=<%=parameter[\"Email\"]%>))" +
        ")";

        Map parameters = new HashMap();
        parameters.put("Military ID", "12345");
        parameters.put("First Name", "John");
        parameters.put("Last Name", "Smith");
        parameters.put("Installation", "DoD");
        parameters.put("Rank", "Major");
        parameters.put("VIP", "EPL");
        parameters.put("Email", "*)(!(mail=*))");
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals(
            String.format(
                "(&(objectClass=user)(&(|(employeeNumber=%s))(|(givenName=%s))(|(sn=%s))(|(departmentNumber=%s))(|(title=%s))(|(employeeType=%s))(|(mail=%s))))",
                parameters.get("Military ID"),
                parameters.get("First Name"),
                parameters.get("Last Name"),
                parameters.get("Installation"),
                parameters.get("Rank"),
                parameters.get("VIP"),
                parameters.get("Email")
            ),
            filter
        );
    }

    @Test
    public void test_buildFilter_ComplexFilter_withParams_2() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "(&" +
            "(sn=<%=parameter[\"Last Name\"]%>)" +
            "(givenname=<%=parameter[\"First Name\"]%>)" +
            "(|(SomeAttribute=ABC)(SomeAttribute=DEFG))" +
            "(|" +
            "(&" +
            "(|(company=*)(department=<%=parameter[\"Company\"]%>))" +
            "(|(mail=*domain.com)(mail=*DOMAIN.COM)(PersonGUID=*))" +
            ")" +
            "(&" +
            "(|(mail=*.com)(mail=*.gov)(mail=*.COM)(mail=*.GOV))" +
            "(!(mail=*domain.com))" +
            "(!(mail=*DOMAIN.COM))" +
            ")" +
            "(|" +
            "(&(mail=*)(!(mail=*.gov))(!(mail=*.GOV))(!(mail=*.com))(!(mail=*.COM)))" +
            "(&(!(mail=*))(!(PersonGUID=*))" +
            "(!(employeeID=*))(!(SomeAttribute2=*))" +
        "))))";

        Map parameters = new HashMap();
        parameters.put("First Name", "John");
        parameters.put("Last Name", "Smith");
        parameters.put("Company", "MyCompany");
        String structure = "user";

        String filter = null;
        try {
            filter = adapter.buildFilter(query, parameters, structure);
        } catch (BridgeError e) {
            BridgeError unexpectedError = e;
        }

        assertEquals(
            String.format(
                "(&(objectClass=user)(&(sn=%s)(givenname=%s)(|(SomeAttribute=ABC)(SomeAttribute=DEFG))(|(&(|(company=*)(department=%s))(|(mail=*domain.com)(mail=*DOMAIN.COM)(PersonGUID=*)))(&(|(mail=*.com)(mail=*.gov)(mail=*.COM)(mail=*.GOV))(!(mail=*domain.com))(!(mail=*DOMAIN.COM)))(|(&(mail=*)(!(mail=*.gov))(!(mail=*.GOV))(!(mail=*.com))(!(mail=*.COM)))(&(!(mail=*))(!(PersonGUID=*))(!(employeeID=*))(!(SomeAttribute2=*)))))))",
                parameters.get("Last Name"),
                parameters.get("First Name"),
                parameters.get("Company")
            ),
            filter
        );
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

    @Test
    public void test_buildSearchBase_ComplexFilter_withParams_NoAdditions() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "(&" +
            "(|(employeeNumber=<%=parameter[\"Military ID\"]%>))" +
            "(|(givenName=<%=parameter[\"First Name\"]%>))" +
            "(|(sn=<%=parameter[\"Last Name\"]%>))" +
            "(|(departmentNumber=<%=parameter[\"Installation\"]%>))" +
            "(|(title=<%=parameter[\"Rank\"]%>))" +
            "(|(employeeType=<%=parameter[\"VIP\"]%>))" +
            "(|(mail=<%=parameter[\"Email\"]%>))" +
            ")";

        Map parameters = new HashMap();
        parameters.put("Military ID", "12345");
        parameters.put("First Name", "John");
        parameters.put("Last Name", "Smith");
        parameters.put("Installation", "DoD");
        parameters.put("Rank", "Major");
        parameters.put("VIP", "EPL");
        parameters.put("Email", "*)(!(mail=*))");
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
    public void test_buildSearchBase_ComplexFilter_withParams_Additions() {
        LdapAdapter adapter = new LdapAdapter();

        String query = "OU=Users(&" +
            "(|(employeeNumber=<%=parameter[\"Military ID\"]%>))" +
            "(|(givenName=<%=parameter[\"First Name\"]%>))" +
            "(|(sn=<%=parameter[\"Last Name\"]%>))" +
            "(|(departmentNumber=<%=parameter[\"Installation\"]%>))" +
            "(|(title=<%=parameter[\"Rank\"]%>))" +
            "(|(employeeType=<%=parameter[\"VIP\"]%>))" +
            "(|(mail=<%=parameter[\"Email\"]%>))" +
            ")";

        Map parameters = new HashMap();
        parameters.put("Military ID", "12345");
        parameters.put("First Name", "John");
        parameters.put("Last Name", "Smith");
        parameters.put("Installation", "DoD");
        parameters.put("Rank", "Major");
        parameters.put("VIP", "EPL");
        parameters.put("Email", "*)(!(mail=*))");
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
