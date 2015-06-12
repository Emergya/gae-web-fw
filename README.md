# GAE-WEB-FW
This project is an Spring based framework for Google Appengine Web Applications written in Java.

It provides base clases for Generic DAO objects that store its associated entity classes (for which provide a base class too), in a way that allows for automatic creation of GAE's search indexes from datastore objects, without need of any programmer action as the search index schemas are created automatically through the use of reflection on the entity classes.

Also, the framework provide some infrastructure to build rest controllers on, with the abilitity to return error custom HTTP codes and custom validation supported throught the use of Spring controller exception handlers (which is in no way limited to GAE in any way).
