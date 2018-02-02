package db61b;
import java.util.HashMap;

/** A collection of Tables, indexed by name.
 *  @author Brandon Griffin*/
class Database {
    /** An empty database. */
    public Database() {
        _hash = new HashMap<>();
    }

    /** Return the Table whose name is NAME stored in this database, or null
     *  if there is no such table. */
    public Table get(String name) {
        return _hash.get(name);
    }

    /** Set or replace the table named NAME in THIS to TABLE.  TABLE and
     *  NAME must not be null, and NAME must be a valid name for a table. */
    public void put(String name, Table table) {
        if (name == null || table == null) {
            throw new IllegalArgumentException("null argument");
        }
        if (_hash.putIfAbsent(name, table) != null) {
            _hash.replace(name, table);
        }
    }
    /** holds names of tables and corresponding tables. */
    private HashMap<String, Table> _hash;
}
