SQL keywords are not always forbidden, because they may be valid in some context and invalid in another one.

The `blocked_db_keywords` file contains the keywords which where historically blocked (formerly `sql_keywords`), completed with extra SQL keywords which make the deployment fail.

SQL keywords which are not known to make the deployment fail are in the `sql_permissive_keywords` file. These are discouraged, but not forbidden.
On the same principles, DBMS-specific keywords which are discouraged are in `h2_permissive_keywords`, `postgres_permissive_keywords`, `mysql_permissive_keywords`, `oracle_permissive_keywords`, `sqlserver_permissive_keywords` files.
