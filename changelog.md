LDAP [bridge-adapters] (2019-11-05)
  * [kinetic-bridgehub-adapter-ldap] 
LDAP [bridge-adapters] (2019-11-05)
  * [kinetic-bridgehub-adapter-ldap] 
    * PER-173 updated the compare subclass to be null safe

LDAP [bridge-adapters] (2020-12-01)
  * [kinetic-bridgehub-adapter-ldap] 
LDAP [bridge-adapters] (2020-12-01)
  * [kinetic-bridgehub-adapter-ldap] 
    * updated pom to use s3 and removed nexus.  Also updated the agent adapter pom config.

LDAP [bridge-adapters] (2021-10-22)
  * [kinetic-bridgehub-adapter-ldap] 
    * PER-237 updated search method to prepend to base when new syntax is leverage.  
    * Added readme and changelog files.

LDAP [bridge-adapters] (2023-05-04)
  * [kinetic-bridgehub-adapter-ldap] 
    * KP-6570 added support for returning ldap attributes the have multiple values.
    * Vector attributes, such as memberOf, would previously return the first member but will now return a JSON array of strings.

LDAP [bridge-adapters] (2023-06-39)
  * [kinetic-bridgehub-adapter-ldap]
    * KP-6837: Do parameter parsing before filter and search base manipulation.  Allows setting queries `(samaccountname=an_id)` where only a parameter is in the qualification `${parameters('a-query')}`.
    * Expanded unit and integration tests.