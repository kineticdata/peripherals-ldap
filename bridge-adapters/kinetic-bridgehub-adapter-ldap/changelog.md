Ldap Adapter  (2019-11-05)
    * PER-173 updated the compare subclass to be null safe

Ldap Adapter v1.0.3 (2020-12-01)
    * updated pom to use s3 and removed nexus.  Also updated the agent adapter pom config.

Ldap Adapter v1.0.4 (2021-10-22)
    * PER-237 updated search method to prepend to base when new syntax is leverage.  This was already done for retrieve and count.
    * Added readme and changelog files. 

Ldap Adapter v1.0.5 (2023-05-04)
  * KP-6570 added support for returning ldap attributes the have multiple values.
  * Vector attributes, such as memberOf, would previously return the first member but will now return a JSON array of strings.
  * This was a fix for USDA.