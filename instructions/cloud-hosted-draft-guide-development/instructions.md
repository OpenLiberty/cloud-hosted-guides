The **COPY** instructions are structured as **COPY** **`[--chown=<user>:<group>]`** **`<source>`** **`<destination>`**. 
They copy local files into the specified destination within your Docker image.
In this case, the **inventory** server configuration files that are located at **src/main/liberty/config** are copied to the **/config/** destination directory.
The **inventory** application WAR file **inventory.war**, which was created from running **mvn package**, is copied to the **/config/apps** destination directory.


[Creating a RESTful web service](https://openliberty.skillsnetwork.site/login?next=/quicklab/cloud-hosted-guide-rest-intro/launch)