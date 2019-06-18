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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chad.rehm
 */
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
    public void testCount() throws Exception{
        BridgeError error = null;
        
        assertNull(error);
        
        // Create the Bridge Request
        List<String> fields = new ArrayList<String>();
        fields.add("sn,mail");
        
        BridgeRequest request = new BridgeRequest();
        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("");
        
        Count count = null;
        try {
            count = getAdapter().count(request);
        } catch (BridgeError e) {
            error = e;
        }
        
        assertNull(error);
        assertTrue(count.getValue() > 0);
    }
    
    @Test
    public void testSearch() throws Exception{
        BridgeError error = null;
        
        assertNull(error);
        
        // Create the Bridge Request
        List<String> fields = new ArrayList<String>();
        fields.add("name");
        
        BridgeRequest request = new BridgeRequest();
        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("");
        
        RecordList list = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            error = e;
        }
        
        assertNull(error);
        assertTrue(list.getRecords().size() > 0);
    } 
    
    @Test
    public void testMultiSearch() throws Exception{
        BridgeError error = null;
        
        assertNull(error);
        
        // Create the Bridge Request
        List<String> fields = new ArrayList<String>();
        fields.add("sn");
        fields.add("mail");
        
        BridgeRequest request = new BridgeRequest();
        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("memberOf=cn=VPN Users,cn=Users,dc=kineticdata,dc=com");
        
        RecordList list = null;
        try {
            list = getAdapter().search(request);
        } catch (BridgeError e) {
            error = e;
        }
        
        assertNull(error);
        assertTrue(list.getRecords().size() > 0);
    }
    
        @Test
    public void test_singleRetrieve() throws Exception {
        BridgeRequest request = new BridgeRequest();
        
        // Create the Bridge Request
        List<String> fields = new ArrayList<String>();
        fields.add("sn");
        fields.add("mail");
        
        request.setStructure("user");
        request.setFields(fields);
        request.setQuery("sAMAccountName=chad.rehm");
        
        Record record = getAdapter().retrieve(request);
        Map<String,Object> recordMap = record.getRecord();
        
        assertNotNull(recordMap);
    }
}
