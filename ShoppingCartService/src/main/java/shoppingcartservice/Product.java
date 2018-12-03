package shoppingcartservice;

public class Product {
    private String navn;
    private int produktID;
    private int pris;


    public int getPris() {
        return pris;
    }

    public void setPris(int pris) {
        this.pris = pris;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public int getProduktID() {
        return produktID;
    }

    public void setProduktID(int produktID) {
        this.produktID = produktID;
    }

    public Product(String navn, int produktID, int pris) {
        this.navn = navn;
        this.produktID = produktID;
        this.pris = pris;
    }
    public Product(){}
}
