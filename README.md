# h2tomysql_for_gerrit #
Tutorial and tool to help those migrating a gerrit instance with an embedded H2 database to one with mysql (mariadb as well)<br>
We use pure java and jdbc to do data transfer, avoid the problem about encoding and parsing.

"*ONLY TESTED WITH VERSION 2.9.1 GERRIT*"<br>

Inspired by original support I found:<br>
https://github.com/scprek/gerrit-h2-mysql

##### Assumptions #####

- This tool assumes you have a current Gerrit instance with H2 running and knowledge of gerrit
- In order to migrate to a mysql based database, some gerrit settings will need to be changed after the migration
- If you would like to go back to the h2 database, you can use your original settings. However be aware you will lose data if you do so after restarting gerrit and data is published to it.

##### Step 1: Download/Install/Update Database (mysql or mariadb) #####

Create reviewdb database

##### Step 2: Initialize a new MySQL database for Gerrit using a temporary site #####
"*ONLY TESTED WITH VERSION 2.9.1 GERRIT*"<br>
"*MAKE SURE YOU USE THE SAME VERSION OF YOUR CURRENT GERRIT INSTANCE*<br>
Just run the standard installation procedure to setup a MySQL instance of Gerrit Code Review in a temporary site path.<br>
You'll wind up with a clean database schema in your MySQL server, and this dummy site directory directory you can clean up later." (from google group forum)<br>

Make sure the gerrit server has the following /etc/gerrit.config<br>
*Note type is mysql even for mariadb
```
[database]
    type = mysql
    hostname = <hostname>
    port = <port>
    database = reviewd
    username = root
```

Shutdown your mysql based temporary gerrit server

##### Step 3: Migration #####
java -cp "bin:lib/*" com.ucweb.gerrit.tools.converter.H2ToMySQLLauncher [path/to/old/gerrit.config] [path/to/new/gerrit.config]

##### Step 4: Update permanent gerrit server's gerrit.config and secure.config #####

See step 2 for gerrit.config options, but do for permanent gerrit instance<br>
As for secure.config add the following
```
[database]
    password = <password>
```

##### Step 5: Restart permanent gerrit server #####

##### Step 6: Clean up #####

You can remove the temporary gerrit site, it was only used to initialize the mysql database

