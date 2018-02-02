package db61b;
import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;

public class TableTests {
    Table t = new Table(new String[]{"Height", "Weight", "NetWorth"});
    @Test
    public void testColumns() {
        assertEquals(1, t.findColumn("Weight"));
        assertEquals(3, t.columns());
        assertEquals("NetWorth", t.getTitle(2));
    }

    @Test
    public void testRows() {
        t.add(new String[]{"56", "184", "40000"});
        t.print();
        t.add(new String[] {"35", "150", "1000"});
        t.print();
        assertEquals(2, t.size());
        assertEquals("35", t.get(0, 0));
        t.add(new String[] {"40", "250", "10000"});
        t.print();
        assertEquals(false, t.add(new String[]{"56", "184", "40000"}));
        assertEquals("Height", t.getTitle(0));
    }

    @Test
    public void testSelect() {
        t.add(new String[]{"56", "184", "40000"});
        t.add(new String[] {"35", "150", "1000"});
        t.add(new String[] {"40", "100", "10000"});
        Column c1 = new Column(t.getTitle(0), t);
        Column c2 = new Column(t.getTitle(1), t);
        Condition test1 = new Condition(c1, "=", "40");
        Condition test2 = new Condition(c2, "<", "200");
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(test1);
        conditions.add(test2);
        ArrayList<String> colNames = new ArrayList<String>();
        colNames.add(t.getTitle(0));
        colNames.add(t.getTitle(1));
        colNames.add(t.getTitle(2));
        Table t2 = t.select(colNames, conditions);
        t2.print();
    }

    @Test
    public void testWrite() {
        t.add(new String[]{"56", "184", "40000"});
        t.writeTable("Dating");
        Table t2 = Table.readTable("Dating");
        assertEquals(1, t2.size());
        assertEquals("56", t2.get(0, 0));
    }
}
