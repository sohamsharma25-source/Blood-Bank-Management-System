package bloodbank.model;
public enum BloodGroup { A_POS, A_NEG, B_POS, B_NEG, AB_POS, AB_NEG, O_POS, O_NEG;
    public static BloodGroup fromNumber(int n) { if(n<1||n>values().length) throw new IllegalArgumentException("Choose 1-8"); return values()[n-1]; }
    public String label(){ return name().replace("_POS","+").replace("_NEG","-"); }
    public static BloodGroup fromLabel(String s){ for(BloodGroup g:values()) if(g.label().equalsIgnoreCase(s)||g.name().equalsIgnoreCase(s)) return g; throw new IllegalArgumentException("Invalid blood group: "+s); }
}