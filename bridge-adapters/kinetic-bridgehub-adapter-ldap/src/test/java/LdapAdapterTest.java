import com.kineticdata.bridgehub.adapter.BridgeAdapterTestBase;
import com.kineticdata.bridgehub.adapter.BridgeError;
import com.kineticdata.bridgehub.adapter.BridgeRequest;
import com.kineticdata.bridgehub.adapter.Count;
import com.kineticdata.bridgehub.adapter.Record;
import com.kineticdata.bridgehub.adapter.RecordList;
import com.kineticdata.bridgehub.adapter.ldap.LdapAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.junit.Test;
import static org.junit.Assert.*;

public class LdapAdapterTest extends BridgeAdapterTestBase {
    
    @Override
    public String getConfigFilePath() {
        return "src/test/resources/bridge-config.yml";
    }
    
    @Override
    public Class getAdapterClass() {
        return LdapAdapter.class;
    }
    
    @Test
    public void testCount() {
        // Create the Bridge Request
        List<String> fields = new ArrayList<>();
        fields.add("sn,mail");
        
        BridgeRequest request = new BridgeRequest();
        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("");
        
        Count count = null;
        BridgeError unexpectedError = null;
        try {
            count = getAdapter().count(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }
        
        assertNull(unexpectedError);
        assertTrue(count.getValue() > 0);
    }
    
    @Test
    public void testSearch() {
        // Create the Bridge Request
        List<String> fields = new ArrayList<>();
        fields.add("memberOf");
        fields.add("cn");
        
        BridgeRequest request = new BridgeRequest();
        request.setStructure("User");
        request.setFields(fields);
        request.setQuery("(sAMAccountName=mary.olowu)");
        
        RecordList list = null;
        BridgeError unexpectedError = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }

        Object str = JSONValue.parse((String)list.getRecords().get(0).getValue("memberOf"));

        assertTrue(str instanceof JSONArray);
        assertNull(unexpectedError);
        assertTrue(list.getRecords().size() > 0);
    }
    
    @Test
    public void testSearchFilter() {
        // Create the Bridge Request
        List<String> fields = new ArrayList<>();
        fields.add("name");
        
        BridgeRequest request = new BridgeRequest();
        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("(samaccountname=*)");
        
        RecordList list = null;
        BridgeError unexpectedError = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }
        
        assertNull(unexpectedError);
        assertTrue(list.getRecords().size() > 0);
    }

    @Test
    public void testSearchFilter_WithoutQuery() {
        // Create the Bridge Request
        List<String> fields = new ArrayList<>();
        fields.add("name");

        BridgeRequest request = new BridgeRequest();
        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("");

        RecordList list = null;
        BridgeError unexpectedError = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }

        assertNull(unexpectedError);
        assertTrue(list.getRecords().size() > 0);
    }

    @Test
    public void testSearchFilter_WithoutAddedSearchBase_WithParens() {
        // Create the Bridge Request
        BridgeRequest request = new BridgeRequest();

        // Add the fields
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("sn");
        request.setFields(fields);

        // Set the Structure
        // This gets appended to the filter as (objectClass=STRUCTURE)
        request.setStructure("User");

        // Set the Query
        request.setQuery("(<%=parameter[\"Search String\"]%>)");

        // Set the Parameters to be replaced in the Query
        Map parameters = new HashMap();
        parameters.put("Search String", "samaccountname=mary.olowu");
        request.setParameters(parameters);

        BridgeError unexpectedError = null;
        RecordList list = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }

        assertNull(unexpectedError);
        assertTrue(list.getRecords().size() > 0);
    }

    @Test
    public void testSearchFilter_WithoutAddedSearchBase_WithoutParensInQuery_WithoutParensInParam() {
        // Create the Bridge Request
        BridgeRequest request = new BridgeRequest();

        // Add the fields
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("sn");
        request.setFields(fields);

        // Set the Structure
        // This gets appended to the filter as (objectClass=STRUCTURE)
        request.setStructure("User");

        // Set the Query
        request.setQuery("<%=parameter[\"Search String\"]%>");

        // Set the Parameters to be replaced in the Query
        Map parameters = new HashMap();
        parameters.put("Search String", "samaccountname=mary.olowu");
        request.setParameters(parameters);

        BridgeError expectedError = null;
        RecordList list = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            expectedError = e;
        }

        assertNotNull(expectedError);
    }

    @Test
    public void testSearchFilter_WithoutAddedSearchBase_WithoutParensInQuery_WithParensInParam() {
        // Create the Bridge Request
        BridgeRequest request = new BridgeRequest();

        // Add the fields
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("sn");
        request.setFields(fields);

        // Set the Structure
        // This gets appended to the filter as (objectClass=STRUCTURE)
        request.setStructure("User");

        // Set the Query
        request.setQuery("<%=parameter[\"Search String\"]%>");

        // Set the Parameters to be replaced in the Query
        Map parameters = new HashMap();
        parameters.put("Search String", "(samaccountname=mary.olowu)");
        request.setParameters(parameters);

        BridgeError unexpectedError = null;
        RecordList list = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }

        assertNull(unexpectedError);
        assertTrue(list.getRecords().size() > 0);
    }

    @Test
    public void testSearchFilter_WithAddedSearchBase() {
        // Create the Bridge Request
        BridgeRequest request = new BridgeRequest();

        // Add the fields
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("sn");
        request.setFields(fields);

        // Set the Structure
        // This gets appended to the filter as (objectClass=STRUCTURE)
        request.setStructure("User");

        // Set the Query
        request.setQuery("OU=Users(<%=parameter[\"Search String\"]%>)");

        // Set the Parameters to be replaced in the Query
        Map parameters = new HashMap();
        parameters.put("Search String", "samaccountname=mary.olowu");
        request.setParameters(parameters);

        BridgeError unexpectedError = null;
        RecordList list = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }

        assertNull(unexpectedError);
        assertTrue(list.getRecords().size() > 0);
    }

    @Test
    public void testSearchFilter_WithAddedSearchBaseAsParam() {
        // Create the Bridge Request
        BridgeRequest request = new BridgeRequest();

        // Add the fields
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("sn");
        request.setFields(fields);

        // Set the Structure
        // This gets appended to the filter as (objectClass=STRUCTURE)
        request.setStructure("User");

        // Set the Query
        request.setQuery("<%=parameter[\"Search Base Append\"]%>(<%=parameter[\"Search String\"]%>)");

        // Set the Parameters to be replaced in the Query
        Map parameters = new HashMap();
        parameters.put("Search String", "samaccountname=mary.olowu");
        parameters.put("Search Base Append", "OU=Users");
        request.setParameters(parameters);

        BridgeError unexpectedError = null;
        RecordList list = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }

        assertNull(unexpectedError);
        assertTrue(list.getRecords().size() > 0);
    }

    @Test
    public void testSearchFilter_WithAddedSearchBase_NoQueryParens() {
        // Create the Bridge Request
        BridgeRequest request = new BridgeRequest();

        // Add the fields
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("sn");
        request.setFields(fields);

        // Set the Structure
        // This gets appended to the filter as (objectClass=STRUCTURE)
        request.setStructure("User");

        // Set the Query
        request.setQuery("<%=parameter[\"Search String\"]%>");

        // Set the Parameters to be replaced in the Query
        Map parameters = new HashMap();
        parameters.put("Search String", "OU=Users(samaccountname=mary.olowu)");
        request.setParameters(parameters);

        BridgeError unexpectedError = null;
        RecordList list = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }

        assertNull(unexpectedError);
        assertTrue(list.getRecords().size() > 0);
    }
    
    @Test
    public void testMultiSearch_withParens() {
        // Create the Bridge Request
        List<String> fields = new ArrayList<>();
        fields.add("sn");
        fields.add("mail");
        
        BridgeRequest request = new BridgeRequest();
        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("(samaccountname=*)");
        
        RecordList list = null;
        BridgeError unexpectedError = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            unexpectedError = e;
        }
        
        assertNull(unexpectedError);
        assertTrue(list.getRecords().size() > 0);
    }

    @Test
    public void testMultiSearch_withoutParens() {
        // Create the Bridge Request
        List<String> fields = new ArrayList<>();
        fields.add("sn");
        fields.add("mail");

        BridgeRequest request = new BridgeRequest();
        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("memberOf=cn=VPN Users,cn=Users,dc=kineticdata,dc=com");

        BridgeError expectedError = null;
        try {
            getAdapter().search(request);
        } catch (BridgeError e) {
            expectedError = e;
        }

        assertNotNull(expectedError);
    }

    @Test
    public void test_singleRetrieve_withParens() throws Exception {
        BridgeRequest request = new BridgeRequest();

        // Create the Bridge Request
        List<String> fields = new ArrayList<>();
        fields.add("sn");
        fields.add("mail");

        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("(sAMAccountName=mary.olowu)");

        Record record = getAdapter().retrieve(request);
        Map<String,Object> recordMap = record.getRecord();

        assertNotNull(recordMap);
    }

    @Test
    public void test_singleRetrieve_withParens_ParamQuery() {
        // Create the Bridge Request
        BridgeRequest request = new BridgeRequest();

        // Add the fields
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("sn");
        request.setFields(fields);

        // Set the Structure
        // This gets appended to the filter as (objectClass=STRUCTURE)
        request.setStructure("User");

        // Set the Query
        request.setQuery("<%=parameter[\"Search String\"]%>");

        // Set the Parameters to be replaced in the Query
        Map parameters = new HashMap();
        parameters.put("Search String", "(samaccountname=mary.olowu)");
        request.setParameters(parameters);

        Map<String, Object> recordMap = null;
        BridgeError unexpectedError = null;
        try {
            Record record = getAdapter().retrieve(request);
            recordMap = record.getRecord();
        } catch (BridgeError e) {
            unexpectedError = e;
        }

        assertNull(unexpectedError);
        assertNotNull(recordMap);
    }

    @Test
    public void test_singleRetrieve_withoutParens_ParamQuery() {
        // Create the Bridge Request
        BridgeRequest request = new BridgeRequest();

        // Add the fields
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("sn");
        request.setFields(fields);

        // Set the Structure
        // This gets appended to the filter as (objectClass=STRUCTURE)
        request.setStructure("User");

        // Set the Query
        request.setQuery("<%=parameter[\"Search String\"]%>");

        // Set the Parameters to be replaced in the Query
        Map parameters = new HashMap();
        parameters.put("Search String", "samaccountname=mary.olowu");
        request.setParameters(parameters);

        Map<String, Object> recordMap = null;
        BridgeError expectedError = null;
        try {
            Record record = getAdapter().retrieve(request);
        } catch (BridgeError e) {
            expectedError = e;
        }

        assertNotNull(expectedError);
    }
    
    @Test
    public void test_singleRetrieve_withoutParens() {
        BridgeRequest request = new BridgeRequest();

        // Create the Bridge Request
        List<String> fields = new ArrayList<>();
        fields.add("sn");
        fields.add("mail");

        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("sAMAccountName=mary.olowu");

        Map<String, Object> recordMap = null;
        BridgeError expectedError = null;
        try {
            Record record = getAdapter().retrieve(request);
        } catch (BridgeError e) {
            expectedError = e;
        }

        assertNotNull(expectedError);
    }

    @Test
    public void test_singleRetrieve_withAddedSearchBase() {
        BridgeRequest request = new BridgeRequest();

        // Create the Bridge Request
        List<String> fields = new ArrayList<>();
        fields.add("sn");
        fields.add("mail");

        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("OU=Users(sAMAccountName=mary.olowu)");

        Map<String, Object> recordMap = null;
        BridgeError unexpectedError = null;
        try {
            Record record = getAdapter().retrieve(request);
            recordMap = record.getRecord();
        } catch (BridgeError e) {
            unexpectedError = e;
        }

        assertNull(unexpectedError);
        assertNotNull(recordMap);
    }
}
