# Ldap Adapter 
The ldap adapter allow for querying an ldap service.
## Configuration Values
| Name                      | Description |
| :------------------------ | :------------------------- |
| Server                    | The domain or IP address of the LDAP service |
| Port                      | The port used to connect to the service |
| Use SSL                   | Determines if TLS will be used |
| Security Principal        | DN of the authenticating user |
| Anonymous Authentication  | Determines if Security Principal and Credentials are required |
| Security Credentials      | password for the authenticating user |
| Search Base               | The base that will be used with every query |
| Page Size                 | The number of records returned on each request |
| Maximum Pages             | The number of total pages that can be fetched |

## Example Configuration
| Name | Value |
| :---- | :--- |
| Server                    | ldap.acme.com |
| Port                      | 389 |
| Use SSL                   | No |
| Security Principal        | cn=Foo,cn=Users,dc=acme,dc=dev |
| Anonymous Authentication  | No |
| Security Credentials      | password |
| Search Base               | dc=acme,dc=dev |
| Page Size                 | 20 |
| Maximum Pages             | 20 |

## Supported Structures
The structure will be used as a filter in the query.  An example of a ldap search query filter that has a structure of `user`: (objectClass=user)

## Fields
Fields that will be returned with the record.  If no fields are provided then all fields will be returned.

## Qualification (Query)
Qualification will be parse to build additional filter or add to the search base

### Example Qualifications
The Structure used is `user`.
* Get users (empty query): ""
* Get users that have mail attribute: `(email=*)`
  * filter: `(&(email=*)(objectClass=user))`
* Prepend to search base: `uid=555`
  * base: `uid=555,dc=acme,dc=dev` 
* Prepend to search base with filter: `uid=555(email=*)`
  * base: `uid=555,dc=acme,dc=dev` 
  * filter: `(&(email=*)(objectClass=user))`

## Important notes

