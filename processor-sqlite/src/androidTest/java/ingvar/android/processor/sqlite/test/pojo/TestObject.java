package ingvar.android.processor.sqlite.test.pojo;

import ingvar.android.literepo.conversion.annotation.Column;
import ingvar.android.literepo.conversion.annotation.Type;
import ingvar.android.processor.sqlite.test.db.TestContract;

/**
 * Created by Igor Zubenko on 2015.04.22.
 */
public class TestObject {

    @Column(value = TestContract.Test.Col._ID, type = Type.INTEGER)
    private Integer id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, type = Type.REAL)
    private Double price;

    public TestObject() {}

    public TestObject(Integer id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof TestObject) {
            TestObject other = (TestObject) object;
            return id.equals(other.id)
                    && name.equals(other.name);
        }
        return false;
    }

}
