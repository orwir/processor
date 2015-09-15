package ingvar.android.processor.filesystem.test.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Igor Zubenko on 2015.04.21.
 */
public class TestObject implements Serializable, Comparable<TestObject> {

    private Integer id;
    private String name;
    private BigDecimal price;

    public TestObject() {}

    public TestObject(Integer id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = new BigDecimal(price);
        this.price.setScale(2, RoundingMode.HALF_UP);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.price.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof TestObject) {
            TestObject other = (TestObject) object;
            return id.equals(other.id)
                && name.equals(other.name)
                && price.equals(other.price);
        }
        return false;
    }

    @Override
    public int compareTo(TestObject another) {
        return id.compareTo(another.id);
    }

}
