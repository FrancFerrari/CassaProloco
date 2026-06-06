package cassaproloco;

import java.io.Serializable;

class Item implements Serializable{
    public int ID = 0;
    public float price = 0;
    public String text = "";
    public String textToPrint = "";
    public int qty = 0;
    
    public Item (int ID, float price, String text, String textToPrint, int qty){
        this.ID=ID;
        this.price=price;
        this.text=text;
        this.textToPrint=textToPrint;
        this.qty=qty;
    }
    
    public Item (){}
    
    @Override
    public String toString(){
        String s;
        s = text + " - " + textToPrint + " - " + price + " €";
        return s;
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Item)) return false;
        
        Item i = (Item) o;
        return i.price == price &&
               i.text.equals(text) &&
               i.textToPrint.equals(textToPrint);
    }
    
    @Override
    public int hashCode(){
        return text.hashCode();
    }
    
    public String getText(){
        return text;
    }
    
    public int getQty(){
        return qty;
    }
    public float getprice(){
        return price;
    }
    public int getID(){
        return ID;
    }
    public void setPrice(float newprice){
        this.price = newprice;
        System.out.println("settato ");
    }
}
