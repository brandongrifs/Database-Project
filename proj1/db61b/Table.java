package db61b;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static db61b.Utils.*;

/** A single table in a database.
 *  @author P. N. Hilfinger
 */
class Table {
    /** A new Table whose columns are given by COLUMNTITLES, which may
     *  not contain duplicate names. */
    Table(String[] columnTitles) {
        if (columnTitles.length == 0) {
            throw error("table must have at least one column");
        }
        _size = 0;
        _rowSize = columnTitles.length;

        for (int i = columnTitles.length - 1; i >= 1; i -= 1) {
            for (int j = i - 1; j >= 0; j -= 1) {
                if (columnTitles[i].equals(columnTitles[j])) {
                    throw error("duplicate column name: %s", columnTitles[i]);
                }
            }
        }
        _titles = columnTitles;
        _columns = new ValueList[_rowSize];
        for (int k = 0; k < _rowSize; k++) {
            _columns[k] = new ValueList();
        }
    }

    /** A new Table whose columns are give by COLUMNTITLES. */
    Table(List<String> columnTitles) {
        this(columnTitles.toArray(new String[columnTitles.size()]));
    }

    /** Return the number of columns in this table. */
    public int columns() {
        return _titles.length;
    }

    /** Return the title of the Kth column.  Requires 0 <= K < columns(). */
    public String getTitle(int k) {
        if (k < 0 || k >= columns()) {
            throw error("index must be within table "
                    + "bounds, 0 <= k < columns()");
        } else {
            return _titles[k];
        }
    }

    /** Return the number of the column whose title is TITLE, or -1 if
     *  there isn't one. */
    public int findColumn(String title) {
        for (int i = columns() - 1; i >= 0; i -= 1) {
            if (_titles[i].equals(title)) {
                return i;
            }
        }
        return -1;
    }

    /** Return the number of rows in this table. */
    public int size() {
        return _size;
    }

    /** Return the value of column number COL (0 <= COL < columns())
     *  of record number ROW (0 <= ROW < size()). */
    public String get(int row, int col) {
        try {
            return _columns[col].get(_index.get(row));
        } catch (IndexOutOfBoundsException excp) {
            throw error("invalid row or column");
        }
    }

    /** Add a new row whose column values are VALUES to me if no equal
     *  row already exists.  Return true if anything was added,
     *  false otherwise. */
    public boolean add(String[] values) {
        String[] testRow = new String[_rowSize];
        for (int i = 0; i < _size; i++) {
            for (int j = 0; j < _rowSize; j++) {
                testRow[j] = this.get(i, j);
            }
            if (Arrays.equals(values, testRow)) {
                return false;
            }
        }
        if (values.length != _columns.length) {
            throw error("Input row has incorrect size");
        } else {
            for (int j = 0; j < columns(); j++) {
                _columns[j].add(values[j]);
            }
            _size++;
            if (_size == 1) {
                _index.add(0, 0);
                return true;
            } else {
                int index = -1;
                search:
                for (int i = 0; i < columns(); i++) {
                    for (int j = 0; j < _index.size(); j++) {
                        if (this.compareRows(_index.get(j), _size - 1) > 0) {
                            index = j;
                            break search;
                        } else if (this.compareRows(_index.get(j), _size - 1)
                                == 0) {
                            break;
                        }
                    }
                }
                if (index < 0) {
                    index = _index.size();
                }
                if (index == _index.size()) {
                    _index.add(index);
                    return true;
                } else {
                    _index.add(index, _size - 1);
                    return true;
                }
            }
        }
    }
    /** Add a new row whose column values are extracted by COLUMNS from
     *  the rows indexed by ROWS, if no equal row already exists.
     *  Return true if anything was added, false otherwise. See
     *  Column.getFrom(Integer...) for a description of how Columns
     *  extract values. */
    public boolean add(List<Column> columns, Integer... rows) {
        String[] values = new String[columns()];
        for (int i = 0; i < columns.size(); i++) {
            values[i] = columns.get(i).getFrom(rows);
        }
        return this.add(values);
    }

    /** Read the contents of the file NAME.db, and return as a Table.
     *  Format errors in the .db file cause a DBException. */
    static Table readTable(String name) {
        BufferedReader input;
        Table table;
        input = null;
        table = null;
        try {
            input = new BufferedReader(new FileReader(name + ".db"));
            String header = input.readLine();
            if (header == null) {
                throw error("missing header in DB file");
            }
            String[] columnNames = header.split(",");
            table = new Table(columnNames);
            String[] values;
            for (String newline = input.readLine(); newline != null;
                 newline = input.readLine()) {
                values = newline.split(",");
                table.add(values);
            }
        } catch (FileNotFoundException e) {
            throw error("could not find %s.db", name);
        } catch (IOException e) {
            throw error("problem reading from %s.db", name);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    /* Ignore IOException */
                }
            }
        }
        return table;
    }

    /** Write the contents of TABLE into the file NAME.db. Any I/O errors
     *  cause a DBException. */
    void writeTable(String name) {
        PrintStream output;
        output = null;
        try {
            String sep;
            sep = "";
            output = new PrintStream(name + ".db");
            output.print(_titles[0]);
            for (int b = 1; b < _titles.length; b++) {
                output.print("," + _titles[b]);
            }
            output.println();
            for (int i = 0; i < _size; i++) {
                output.print(_columns[0].get(i));
                for (int k = 1; k < _rowSize; k++) {
                    output.print("," + _columns[k].get(i));
                }
                output.println();
            }
        } catch (IOException e) {
            throw error("trouble writing to %s.db", name);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /** Print my contents on the standard output, separated by spaces
     *  and indented by two spaces. */
    void print() {
        PrintStream output = new PrintStream(System.out);
        for (int j = 0; j < _size; j++) {
            output.print(" ");
            for (int i = 0; i < _rowSize; i++) {
                output.print(" " + _columns[i].get(_index.get(j)));
            }
            output.println();
        }
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected from
     *  rows of this table that satisfy CONDITIONS. */
    Table select(List<String> columnNames, List<Condition> conditions) {
        Table result = new Table(columnNames);
        ArrayList<Column> c = new ArrayList<Column>();
        for (String s : columnNames) {
            c.add(new Column(s, this));
        }
        if (conditions.isEmpty()) {
            for (int i = 0; i < _size; i++) {
                String[] rowVals = new String[columnNames.size()];
                for (int k = 0; k < columnNames.size(); k++) {
                    rowVals[k] = (c.get(k).getFrom(i));
                }
                result.add(rowVals);
            }
        } else {
            for (int i = 0; i < _size; i++) {
                String[] rowVals = new String[columnNames.size()];
                if (Condition.test(conditions, i)) {
                    for (int k = 0; k < columnNames.size(); k++) {
                        rowVals[k] = (c.get(k).getFrom(i));
                    }
                    result.add(rowVals);
                }
            }
        }
        return result;
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected
     *  from pairs of rows from this table and from TABLE2 that match
     *  on all columns with identical names and satisfy CONDITIONS. */
    Table select(Table table2, List<String> columnNames,
                 List<Condition> conditions) {
        Table result = new Table(columnNames);
        ArrayList<Column> c = new ArrayList<Column>();
        for (int i = 0; i < columnNames.size(); i++) {
            c.add(new Column(columnNames.get(i), this, table2));
        }
        ArrayList<Column> common = new ArrayList<Column>();
        ArrayList<Column> that = new ArrayList<Column>();
        for (String title1 : this._titles) {
            for (String title2 : table2._titles) {
                if (title1.equals(title2)) {
                    common.add(new Column(title1, this, table2));
                    that.add(new Column(title1, table2, this));
                }
            }
        }
        ArrayList<String> values = new ArrayList<String>();
        for (int i = 0; i < this.size(); i++) {
            for (int j = 0; j < table2.size(); j++) {
                if (equijoin(common, that, i, j)
                        && Condition.test(conditions, i, j)) {
                    result.add(c, i, j);
                }
            }
        }
        return result;
    }

    /** Return <0, 0, or >0 depending on whether the row formed from
     *  the elements _columns[0].get(K0), _columns[1].get(K0), ...
     *  is less than, equal to, or greater than that formed from elememts
     *  _columns[0].get(K1), _columns[1].get(K1), ....  This method ignores
     *  the _index. */
    private int compareRows(int k0, int k1) {
        for (int i = 0; i < _columns.length; i += 1) {
            int c = _columns[i].get(k0).compareTo(_columns[i].get(k1));
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    /** Return true if the columns COMMON1 from ROW1 and COMMON2 from
     *  ROW2 all have identical values.  Assumes that COMMON1 and
     *  COMMON2 have the same number of elements and the same names,
     *  that the columns in COMMON1 apply to this table, those in
     *  COMMON2 to another, and that ROW1 and ROW2 are indices, respectively,
     *  into those tables. */
    private static boolean equijoin(List<Column> common1, List<Column> common2,
                                    int row1, int row2) {
        if (common1.size() == 0 || common2.size() == 0) {
            return true;
        }
        for (Column c1 : common1) {
            for (Column c2 : common2) {
                if (c1.getName().equals(c2.getName())) {
                    if (!c1.getFrom(row1).equals(c2.getFrom(row2))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** A class that is essentially ArrayList<String>.  For technical reasons,
     *  we need to encapsulate ArrayList<String> like this because the
     *  underlying design of Java does not properly distinguish between
     *  different kinds of ArrayList at runtime (e.g., if you have a
     *  variable of type Object that was created from an ArrayList, there is
     *  no way to determine in general whether it is an ArrayList<String>,
     *  ArrayList<Integer>, or ArrayList<Object>).  This leads to annoying
     *  compiler warnings.  The trick of defining a new type avoids this
     *  issue. */
    private static class ValueList extends ArrayList<String> {
    }

    /** My column titles. */
    private final String[] _titles;
    /** My columns. Row i consists of _columns[k].get(i) for all k. */
    private final ValueList[] _columns;

    /** Rows in the database are supposed to be sorted. To do so, we
     *  have a list whose kth element is the index in each column
     *  of the value of that column for the kth row in lexicographic order.
     *  That is, the first row (smallest in lexicographic order)
     *  is at position _index.get(0) in _columns[0], _columns[1], ...
     *  and the kth row in lexicographic order in at position _index.get(k).
     *  When a new row is inserted, insert its index at the appropriate
     *  place in this list.
     *  (Alternatively, we could simply keep each column in the proper order
     *  so that we would not need _index.  But that would mean that inserting
     *  a new row would require rearranging _rowSize lists (each list in
     *  _columns) rather than just one. */
    private final ArrayList<Integer> _index = new ArrayList<>();

    /** My number of rows (redundant, but convenient). */
    private int _size;
    /** My number of columns (redundant, but convenient). */
    private final int _rowSize;
}
