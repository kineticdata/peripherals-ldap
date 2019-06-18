/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kineticdata.bridgehub.adapter.ldap;

// Import the necessary core Java classes
import java.util.*;
import java.text.*;
// Import the classes necessary for communicating with Ldap
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
// Import the log4j library
import org.apache.commons.lang.StringUtils;
// Import the Kinetic Bridge Bootstrap library
import com.kineticdata.bridgehub.adapter.BridgeAdapter;
import com.kineticdata.bridgehub.adapter.BridgeError;
import com.kineticdata.bridgehub.adapter.BridgeRequest;
import com.kineticdata.bridgehub.adapter.Count;
import com.kineticdata.bridgehub.adapter.Record;
import com.kineticdata.bridgehub.adapter.RecordList;
// import Matcher and Pattern
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// Import servlet libraries

import com.kineticdata.commons.v1.config.ConfigurableProperty;
import com.kineticdata.commons.v1.config.ConfigurablePropertyMap;
import java.io.IOException;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class LdapAdapter implements BridgeAdapter {
    /*---------------------------------------------------------------------------------------------
     * SETUP METHODS
     *-------------------------------------------------------------------------------------------*/
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String getVersion() {
       return VERSION;
    }
    
    @Override
    public ConfigurablePropertyMap getProperties() {
        return properties;
    }
    
    @Override
    public void setProperties(Map<String,String> parameters) {
        properties.setValues(parameters);
    }
     
    @Override    
    public void initialize() throws BridgeError {
        ConfigurablePropertyMap configuration = getProperties();
        // Add the default environment configuation values
        environment.put(Context.INITIAL_CONTEXT_FACTORY, Properties.ENVIRONMENT_INITIAL_CONTEXT_FACTORY);
        environment.put(Context.REFERRAL, Properties.ENVIRONMENT_REFERRAL);
        environment.put(Context.SECURITY_AUTHENTICATION, configuration.getValue(Properties.PROPERTY_SECURITY_ANONYMOUS).equalsIgnoreCase("no") ? "simple" : "none");
        // Configure the environment hashtable (this is used to create the
        // LdapContext object responsible for interacting with the server).
        environment.put(Context.PROVIDER_URL, (configuration.getValue(Properties.PROPERTY_SSL).equalsIgnoreCase("no") ? "ldap://" : "ldaps://") +
            configuration.getValue(Properties.PROPERTY_SERVER)+":"+configuration.getValue(Properties.PROPERTY_PORT));
        environment.put(Context.SECURITY_PRINCIPAL, configuration.getValue(Properties.PROPERTY_SECURITY_PRINCIPAL));
        environment.put(Context.SECURITY_CREDENTIALS, configuration.getValue(Properties.PROPERTY_SECURITY_CREDENTIALS));
        // Set the search base
        this.searchBase = configuration.getValue(Properties.PROPERTY_SEARCH_BASE);

        this.maximumPages = Integer.valueOf(configuration.getValue(Properties.PROPERTY_MAXIMUM_PAGES));
        this.pageSize = Integer.valueOf(configuration.getValue(Properties.PROPERTY_PAGE_SIZE));
        
        // If the username or password are blank and the anonymous authentication is set to 'no', throw an error
        if (
            configuration.getValue(Properties.PROPERTY_SECURITY_ANONYMOUS).equalsIgnoreCase("no") && 
                (
                StringUtils.isBlank(configuration.getValue(Properties.PROPERTY_SECURITY_PRINCIPAL)) ||
                StringUtils.isBlank(configuration.getValue(Properties.PROPERTY_SECURITY_CREDENTIALS))
                )
        ) {
            throw new BridgeError("Blank security principal or credentials.");
        }

        // Validate the environmental configuration (server connectivity,
        // credentials, etc).
        try {
            new InitialLdapContext(environment, null);
        } catch (AuthenticationException e) {
            throw new BridgeError("Unable to authenticate using the provided credentials.", e);
        } catch (CommunicationException e) {
            throw new BridgeError("Unable to connect to the specified LDAP server.", e);
        } catch (Exception e) {
            throw new BridgeError("Unknown exception: "+e.getMessage(), e);
        }
    }


    /*----------------------------------------------------------------------------------------------
     * PROPERTIES
     *--------------------------------------------------------------------------------------------*/
    
    /** Defines the adapter display name. */
    public static final String NAME = "Ldap Bridge";
    
    /** Defines the logger */
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(LdapAdapter.class);
    
    /** Adapter version constant. */
    public static String VERSION;
    /** Load the properties version from the version.properties file. */
    static {
        try {
            java.util.Properties properties = new java.util.Properties();
            properties.load(LdapAdapter.class.getResourceAsStream("/"+LdapAdapter.class.getName()+".version"));
            VERSION = properties.getProperty("version");
        } catch (IOException e) {
            logger.warn("Unable to load "+LdapAdapter.class.getName()+" version properties.", e);
            VERSION = "Unknown";
        }
    }
    
    /** Defines the collection of property names for the adapter. */
    public static class Properties {
        // Define the date formats
        public static final String LDAP_DATE_FORMAT = "yyyyMMddHHmmss";
        public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

        // Define the environmental constants
        public static final String ENVIRONMENT_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
        public static final String ENVIRONMENT_REFERRAL = "follow";
        // Specify the property name constants
        public static final String PROPERTY_SERVER = "Server";
        public static final String PROPERTY_PORT = "Port";
        public static final String PROPERTY_SSL = "Use SSL";
        public static final String PROPERTY_SECURITY_ANONYMOUS = "Anonymous Authentication";
        public static final String PROPERTY_SECURITY_PRINCIPAL = "Security Principal";
        public static final String PROPERTY_SECURITY_CREDENTIALS = "Security Credentials";
        public static final String PROPERTY_SEARCH_BASE = "Search Base";
        public static final String PROPERTY_PAGE_SIZE = "Page Size";
        public static final String PROPERTY_MAXIMUM_PAGES = "Maximum Pages";
    }

    /**
     * Specify the name, type, and default values for the configurable
     * properties.  When using the Kinetic Bridge Bootstrap, these will be
     * displayed as configurable values within the AdminConsole.
     */
    private final ConfigurablePropertyMap properties = new ConfigurablePropertyMap(
        new ConfigurableProperty(Properties.PROPERTY_SERVER).setValue("127.0.0.1"),
        new ConfigurableProperty(Properties.PROPERTY_PORT).setValue("389"),
        new ConfigurableProperty(Properties.PROPERTY_SSL).addPossibleValues("Yes","No").setValue("No"),
        new ConfigurableProperty(Properties.PROPERTY_SECURITY_ANONYMOUS).addPossibleValues("Yes","No").setValue("No"),
        new ConfigurableProperty(Properties.PROPERTY_SECURITY_PRINCIPAL).setValue("CN=USERNAME,CN=USERS,DC=DOMAIN,DC=com"),
        new ConfigurableProperty(Properties.PROPERTY_SECURITY_CREDENTIALS).setIsSensitive(true),
        new ConfigurableProperty(Properties.PROPERTY_SEARCH_BASE).setValue("DC=DOMAIN,DC=com"),
        new ConfigurableProperty(Properties.PROPERTY_PAGE_SIZE).setValue("50"),
        new ConfigurableProperty(Properties.PROPERTY_MAXIMUM_PAGES).setValue("20")
    );

    // Define the constants that are helpful
    public static final String SYNTAX_GENERALIZED_TIME = "1.3.6.1.4.1.1466.115.121.1.24";

    // Define the local LDAP structure caches
    private Map<String,String> attributeSyntaxMap = new LinkedHashMap();
    private Map<String,List<String>> structureMap = new LinkedHashMap();

    // Define the bridge variables
    private Integer pageSize;
    private Integer maximumPages;
    private String searchBase;
    private Hashtable<String,String> environment = new Hashtable();

    /**
     *
     * @param request
     * @return
     * @throws BridgeError
     */
    @Override
    public Count count(BridgeRequest request) throws BridgeError {
        // Build the context
        LdapContext context = buildContext(environment);
        // Build the query filter
        String filter = buildFilter(request);

        // Prepend the search base that was sent in the query (if applicable)
        String newSearchBase = request.getQuery().replaceAll("\\(.*\\)", "");
        String fullSearchBase;
        LdapQualificationParser parser = new LdapQualificationParser();
        if (StringUtils.isNotBlank(newSearchBase)) {
            fullSearchBase = parser.parse(newSearchBase, request.getParameters()) + "," + this.searchBase;
        } else {
            fullSearchBase = this.searchBase;
        }
        Long count = 0L;
        logger.trace("  Query with parameter values: " + filter);

        // Try to execute the query
        try {
            // Build up the search controls
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration results = context.search(fullSearchBase, filter, controls);
            while(results.hasMore() && results.next() != null) {count++;}
        }
        // If there was a problem retrieving the records
        catch (NamingException e) {
            throw new BridgeError("There was a problem searching LDAP: "+e.getMessage(),e);
        }

        // Ensure the context is closed
        try {
            context.close();
        } catch (NamingException e) {
            throw new BridgeError("There was a problem closing the connection.", e);
        }

        // Return the result data string
        return new Count(count);
    }

    /**
     *
     * @param request
     * @return
     * @throws BridgeError
     */
    @Override
    public Record retrieve(BridgeRequest request) throws BridgeError {
        // Build the context
        LdapContext context = buildContext(environment);
        // Build the query filter
        String filter = buildFilter(request);

        // Prepend the search base that was sent in the query (if applicable)
        String newSearchBase = request.getQuery().replaceAll("\\(.*\\)", "");
        String fullSearchBase;
        LdapQualificationParser parser = new LdapQualificationParser();
        if (StringUtils.isNotBlank(newSearchBase)) {
            fullSearchBase = parser.parse(newSearchBase, request.getParameters()) + "," + this.searchBase;
        } else {
            fullSearchBase = this.searchBase;
        }
        
        logger.trace("  Query with parameter values: " + filter);
        // Initialize the result record
        Map<String,Object> record = null;
        // Try to execute the query
        try {
            // Initialize the list of fields
            List<String> fields = request.getFields();
            if (fields == null) {
                fields = getStructureFields(request.getStructure());
            }

            // Build up the search controls, sorting will be based on the order
            // that the attributes were requested
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(fields.toArray(new String[fields.size()]));

            // Retrieve the search result and throw an exception if there are multiple
            SearchResult result = null;
            try {
                // Query for results
                NamingEnumeration searchResults = context.search(fullSearchBase, filter, controls);
                // If there is at least one search result
                if (searchResults.hasMore()) {
                    // Set the result
                    result = (SearchResult)searchResults.next();
                    // If there was more than one search result
                    if (searchResults.hasMore()) {
                        throw new BridgeError("Multiple results matched the "+
                            "retrieve request (single result expected).");
                    }
                }
            }
            // If there was a problem retrieving the result
            catch (NamingException e) {
                throw new BridgeError("Unable to retrieve search results for: "+filter, e);
            }
            // If there was a search result found
            if (result != null) {
                record = buildRecordMap(fields, result);
            }
        } catch (NamingException e) {
            throw new BridgeError("There was a problem searching LDAP: "+e.getMessage(),e);
        }

        // Ensure the context is closed
        try {
            context.close();
        } catch (NamingException e) {
            throw new BridgeError("There was a problem closing the connection.", e);
        }

        // Return the result data string
        return new Record(record);
    }

    @Override
    public RecordList search(BridgeRequest request) throws BridgeError {
        // Build the context
        InitialLdapContext context = buildContext(environment);
        // Build the query filter
        String filter = buildFilter(request);

        // Prepend the search base that was sent in the query (if applicable)
        String newSearchBase = request.getQuery().replaceAll("\\(.*\\)", "");
        String fullSearchBase;
        LdapQualificationParser parser = new LdapQualificationParser();
        if (StringUtils.isNotBlank(newSearchBase)) {
            fullSearchBase = parser.parse(newSearchBase, request.getParameters()) + "," + this.searchBase;
        } else {
            fullSearchBase = this.searchBase;
        }
        
        logger.trace("  Query with parameter values: " + filter);
        
        // Initialzie the records list
        List<Record> records = new ArrayList<Record>();
        
        // Build the metadata
        Map<String,String> metadata = new LinkedHashMap();

        // Try to execute the query
        try {
            // Initialize the list of fields
            List<String> fields = request.getFields();
            if (fields == null) {
                fields = getStructureFields(request.getStructure());
            }

            // Build up the search controls
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            // Build an array of fields for the object
            String[] fieldsArray = fields.toArray(new String[fields.size()]);
            // Set the returning attributes
            controls.setReturningAttributes(fieldsArray);

            // Set up the page size
            context.setRequestControls(new Control[]{new PagedResultsControl(pageSize, Control.CRITICAL) });

            byte[] cookie = null;
            int page = 0;

            while (page == 0 || (page < maximumPages && cookie != null)) {
                // Retrieve the search results
                NamingEnumeration<SearchResult> searchResults;
                try {
                    searchResults = context.search(searchBase, filter, controls);
                } catch (NamingException e) {
                    throw new BridgeError("Unable to retrieve search results for: "+filter, e);
                }

                // For each of the returned results
                while (searchResults.hasMore()) {
                    // Add the record to the list of records
                    records.add(new Record(buildRecordMap(fields, searchResults.next())));
                }

                // Examine the paged results control response
                Control[] responseControls = context.getResponseControls();

                if (responseControls != null) {
                    for (int i = 0; i < responseControls.length; i++) {
                        if (responseControls[i] instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) responseControls[i];
                            cookie = prrc.getCookie();
                        }
                    }
                }

                // Increment chunk
                page++;

                // Re-activate paged results
                context.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });
            }
            
            // Sort the list
            Collections.sort(records, new RecordComparator(fields));
            
            metadata.put("size", String.valueOf(records.size()));
            if (records.size() == page*pageSize) {
                metadata.put("limitReached", "true");
            }

        } catch (java.io.IOException e) {
            throw new BridgeError("There was a problem searching LDAP: "+e.getMessage(),e);
        } catch (NamingException e) {
            throw new BridgeError("There was a problem searching LDAP: "+e.getMessage(),e);
        }

        // Ensure the context is closed
        try {
            context.close();
        } catch (NamingException e) {
            throw new BridgeError("There was a problem closing the connection.", e);
        }

        // Return the response value
        return new RecordList(request.getFields(), records, metadata);

    }

    /***************************************************************************
     * HELPER ACCESSOR METHODS
     **************************************************************************/

    /**
     * Returns the LDAP attribute type OID for the specified attribute name.
     *
     * Example:
     *   LdapBridge bridge = new LdapBridge(configuration);
     *   String oid = bridge.getStructureFieldSyntax("whenCreated");
     *
     * oid == "1.3.6.1.4.1.1466.115.121.1.24", which represents an LDAP
     * "Generalized Time" type.
     *
     * @param field
     * @return
     * @throws BridgeError
     */
    private String getStructureFieldSyntax(String field) throws BridgeError {
        // Initialize the result
        String result = attributeSyntaxMap.get(field);

        // If the cache does not yet have the syntax for the attribute
        if (result == null) {
            // Build the context
            InitialLdapContext context = buildContext(environment);

            // Attempt to retrieve the structure fields
            try {
                // Get the root schema
                DirContext schema = context.getSchema("");
                // Get the attributes that specify the definition of the specified attribute
                Attributes definition = schema.getAttributes("AttributeDefinition/"+field);

                // Retrieve the syntax type and super attribute
                Attribute syntaxAttribute = definition.get("syntax");
                Attribute supAttribute = definition.get("sup");
                // While the attribute defition does not have a syntax and does
                // have a SUPerior type, walk up the attribute definition chain
                while (syntaxAttribute == null && supAttribute != null) {
                    definition = schema.getAttributes("AttributeDefinition/"+supAttribute.get());
                    syntaxAttribute = definition.get("syntax");
                    supAttribute = definition.get("sup");
                }

                // If a syntax attribute was found
                if (syntaxAttribute != null) {
                    result = syntaxAttribute.get().toString();
                }
                // If the syntax attribute was not found
                else {
                    throw new BridgeError("Unable to determine proper field syntax for field: "+field);
                }
            }
            // Wrap any LDAP exceptions in a BridgeError
            catch (NamingException e) {
                throw new BridgeError("There was a problem retrieving the structure "+
                    "field definition for the '"+field+"' attribute.", e);
            }

            // Add the result to the cache
            attributeSyntaxMap.put(field, result);
        }

        // Return the result
        return result;
    }

    /**
     * 
     * @return
     * @throws BridgeError
     */
    public List<String> getStructureFields(String structure) throws BridgeError {
        // Attempt to retrieve the result from the cached structure map
        List<String> result = structureMap.get(structure);

        // If the result was not cached in the results map
        if (result == null) {
            logger.info("Retrieving structure fields for: "+structure);

            // Initialize a new result
            result = new ArrayList();
            // Build the context
            InitialLdapContext context = buildContext(environment);
            // Attempt to retrieve the structure fields
            try {
                // Get the root schema
                DirContext schema = context.getSchema("");
                // Initialize a set to temporarily store attributes
                Set<String> attributes = new LinkedHashSet();
                // Retrieve all of the attributes for the specified object class
                String className = structure;
                // Add each of the optional and required attributes for the object
                // class, and each of its super classes, until we reach the root
                // object class (top).
                while(!"top".equals(className)) {
                    logger.debug("  "+className);

                    // Retrieve the definition for the attribute
                    Attributes definition = schema.getAttributes("ClassDefinition/"+className);

                    // Retrieve the optional attributes
                    Attribute optionalAttributes = definition.get("may");
                    // Add each of the optional attributes to the attributes
                    if (optionalAttributes != null) {
                        NamingEnumeration optionalAttributeNames = optionalAttributes.getAll();
                        while(optionalAttributeNames.hasMore()) {
                            String name = optionalAttributeNames.next().toString();
                            logger.trace("    Optional: "+name);
                            attributes.add(name);
                        }
                    }

                    // Retrieve the required attributes
                    Attribute requiredAttributes = definition.get("must");
                    // Add each of the requried attributes to the attributes
                    if (requiredAttributes != null) {
                        NamingEnumeration requiredAttributeNames = requiredAttributes.getAll();
                        while(requiredAttributeNames.hasMore()) {
                            String name = requiredAttributeNames.next().toString();
                            logger.trace("    Required: "+name);
                            attributes.add(name);
                        }
                    }

                    // Retrieve the parent class name
                    className = definition.get("sup").get().toString();
                }
                // Initialize a set to temporarily store attributes
                result.addAll(attributes);
            }
            // Wrap any LDAP exceptions in a BridgeError
            catch (NamingException e) {
                throw new BridgeError("There was a problem retrieving the structure "+
                    "fields for the '"+structure+"' objectClass.", e);
            }

            // Sort the results
            Collections.sort(result);
            
            // Add the result to the strucureMapCache
            structureMap.put(structure, result);
        }

        // Return the sorted result
        return result;
    }

    /**
     *
     * @return
     * @throws BridgeError
     */
    public List<String> getStructures() throws BridgeError {
        // Initialize the result
        List<String> result = new ArrayList();

        // Build the context
        InitialLdapContext context = buildContext(environment);

        // Attempt to retrieve the list of objectClass definitions
        try {
            // Get the root schema
            DirContext schema = context.getSchema("");
            // Retrieve the enumeration of ClassDefinition bindings
            NamingEnumeration<Binding> bindings = schema.listBindings("ClassDefinition");
            // For each of the class definitions
            while (bindings.hasMore()) {
                Binding binding = bindings.next();
                result.add(binding.getName());
            }
        }
        // Wrap any LDAP exceptions in a BridgeError
        catch (NamingException e) {
            throw new BridgeError("There was a problem retrieving the list "+
                "of available structures.", e);
        }

        // Ensure the results are sorted
        Collections.sort(result);

        // Return the sorted results
        return result;
    }

    /***************************************************************************
     * INTERNAL HELPER METHODS
     **************************************************************************/

    private InitialLdapContext buildContext(Hashtable<String,String> environment) throws BridgeError {
        // Declare the context
        InitialLdapContext context;
        // Try to build the context
        
        try {
            context = new InitialLdapContext(environment, null);
        } catch (AuthenticationException e) {
            throw new BridgeError("Unable to authenticate using the provided credentials.", e);
        } catch (CommunicationException e) {
            throw new BridgeError("Unable to connect to the specified LDAP server.", e);
        } catch (Exception e) {
            throw new BridgeError("Unknown exception: "+e.getMessage(), e);
        }
        // Return the context
        return context;
    }

    private String buildFilter(BridgeRequest request) throws BridgeError {
        Pattern pattern = Pattern.compile("\\(.*\\)");
        Matcher matcher = pattern.matcher(request.getQuery());
        
        // Using a regex to filter out the part of the requestQuery that will
        // used for the filter and the part that will be added to the search base
        String filterQuery = "";
        if (matcher.find()) {
            filterQuery = matcher.group();
        }
                
        // Merge the object class filter segment with the desired filter
        String query = "(objectClass="+request.getStructure()+")";
        if (StringUtils.isNotBlank(filterQuery)) {
            query = "(&"+query+filterQuery+")";
        }

        // Initialize a new qualification parser to automatically replace
        // parameter values
        LdapQualificationParser parser = new LdapQualificationParser();
        String filter = parser.parse(query, request.getParameters());

        // Returned the query string
        return filter;
    }

    private Map<String,Object> buildRecordMap(List<String> fields, SearchResult entry) throws BridgeError, NamingException {
        // Initialize the result
        Map<String,Object> result = new LinkedHashMap();

        // Retrieve the result attributes
        Attributes attributes = entry.getAttributes();

        // For each of the requested fields
        for(String name : fields) {
            // Initialize the value
            String value = null;

            // Retrieve the attribute associated with the field name
            Attribute attribute = attributes.get(name);

            // If the attribute is not null
            if (attribute != null) {
                // Retrieve the attribute syntax (IE the field type)
                String syntax = getStructureFieldSyntax(name);

                // If the attribute is a "Generalize Time", translate it to ISO8601
                if (SYNTAX_GENERALIZED_TIME.equals(syntax)) {
                    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                    SimpleDateFormat arsFormat = new SimpleDateFormat(Properties.LDAP_DATE_FORMAT);
                    arsFormat.setCalendar(calendar);
                    SimpleDateFormat iso8601Format = new SimpleDateFormat(Properties.DATE_FORMAT);
                    iso8601Format.setCalendar(calendar);
                    try {
                        Date date = arsFormat.parse(attribute.get().toString());
                        value = iso8601Format.format(date);
                    } catch (ParseException e) {
                        throw new BridgeError("Unable to parse date value: "+value, e);
                    }
                }
                // If the attribute is not a type that needs translating
                else {
                    // Set the value to be the string value
                    value = attribute.get().toString();
                }
            }

            // Att the att
            result.put(name, value);
        }

        // Return the ercord map
        return result;
    }

    /**
     * Internal class used to compare and sort records.
     */
    private static class RecordComparator implements Comparator<Record> {
        private List<String> fields;

        public RecordComparator(List<String> fields) {
            this.fields = fields;
        }
        
        @Override
        public int compare( Record record1, Record record2) {
            int comparison = 0;
            for(String field : fields) {
                String value1 = record1.getValue(field).toString();
                String value2 = record2.getValue(field).toString();
                // If both records are null, continue with the comparison
                if (value1 == null && value2 == null) {continue;}
                // If only value1 is null, return a negative number (indicating
                // that record1 is "less than" record2).
                else if (value1 == null) {
                    comparison = -1;
                    break;
                }
                // If only value2 is null, return a positive number (indicating
                // that record1 is "greater than" record 2);
                else if (value2 == null) {
                    comparison = 1;
                    break;
                }
                // If the values are equal, continue to the next comparison
                else if (value1.compareTo(value2) == 0) {continue;}
                // If the values are not equal, return the result of the
                // comparison
                else {
                    comparison = value1.compareTo(value2);
                    break;
                }
            }
            return comparison;
        }
//        
//        @Override
//        public int compare(Object record1, Object record2) {
//            try {
//                return compare((Map<String,String>)record1, (Map<String,String>)record2);
//            } catch (ClassCastException e) {
//                throw new RuntimeException("Unable to compare objects not of type Map<String,String>.", e);
//            }
//        }
    }
}