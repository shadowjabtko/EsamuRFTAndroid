package hu.esamu.rft.esamurft.inventory;


public class Item {
    private int id;
    private String name;
    private int number;
    private String image;

    public Item(int id, String name, int number, String image) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public String getImage() {
        return image;
    }
}
