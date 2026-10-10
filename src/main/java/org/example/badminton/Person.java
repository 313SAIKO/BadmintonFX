package org.example.badminton;

// abstract class: ต้นแบบของบุคคลในระบบ สร้าง object จาก Person โดยตรงไม่ได้
public abstract class Person {
    private String id;
    private String name;
    private String phone;

    public Person(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }

    // abstract method: คลาสลูกทุกตัวต้อง override
    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " " + id + " " + name + " (โทร " + phone + ")";
    }
}
