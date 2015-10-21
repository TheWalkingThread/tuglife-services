package net.thewalkingthread.tuglifeservices;

import com.google.appengine.api.datastore.*;
import net.thewalkingthread.tuglifeservices.payload.MathUser;

public class DataStoreHandler {

    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public static void createMathUser(MathUser usr, String type){
        Entity user = new Entity(type);
        user.setProperty("username", usr.name);
        user.setProperty("matnum", usr.matnum);
        user.setProperty("pw", usr.pw);

        datastore.put(user);
    }

    public static MathUser getMathUser(String name, String type){
        Query.Filter nameFilter = new Query.FilterPredicate("username", Query.FilterOperator.EQUAL, name);
        Query q = new Query(type).setFilter(nameFilter);
        PreparedQuery pq = datastore.prepare(q);

        Entity ent = pq.asSingleEntity();
        if (ent != null){
            String matnum = (String) ent.getProperty("matnum");
            String pw_math = (String) ent.getProperty("pw");

            return new MathUser(name, matnum, pw_math);
        } else {
            return null;
        }
    }

    public static void updateMathUser(MathUser newuser, String type){
        Query.Filter nameFilter = new Query.FilterPredicate("username", Query.FilterOperator.EQUAL, newuser.name);
        Query q = new Query(type).setFilter(nameFilter);

        PreparedQuery pq = datastore.prepare(q);

        Entity ent = pq.asSingleEntity();

        ent.setProperty("username", newuser.name);
        ent.setProperty("matnum", newuser.matnum);
        ent.setProperty("pw", newuser.pw);

        datastore.put(ent);
    }
}
