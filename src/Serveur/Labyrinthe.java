package Serveur;

public class Labyrinthe {

    private int h; // nb de lignes
    private int w; // nb de colonnes
    private Case[][] laby;

    Labyrinthe() {
        createLaby1();
        h = 20;
        w = 20;
        printLaby();
    }

    /*
     * -----
     * Fonctions
     * -----
     */

    public void printLaby(){
        for (int i = 0; i < 20; i++){
            for (int j = 0; j < 20; j++){
                if (laby[i][j].isMur()){
                    System.out.print("X");
                }else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    public void createLine(int i , boolean b0, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7, boolean b8, boolean b9, 
    boolean b10, boolean b11, boolean b12, boolean b13, boolean b14, boolean b15, boolean b16, boolean b17, boolean b18, boolean b19){

        laby[i][0] = new Case(b0); laby[i][1] = new Case(b1); laby[i][2] = new Case(b2); laby[i][3] = new Case(b13); 
        laby[i][4] = new Case(b4); laby[i][5] = new Case(b5); laby[i][6] = new Case(b6); laby[i][7] = new Case(b7); 
        laby[i][8] = new Case(b8); laby[i][9] = new Case(b9); laby[i][10] = new Case(b10);laby[i][11] = new Case(b11); 
        laby[i][12] = new Case(b12);laby[i][13] = new Case(b13);laby[i][14] = new Case(b14); laby[i][15] = new Case(b15);
        laby[i][16] = new Case(b16);laby[i][17] = new Case(b17); laby[i][18] = new Case(b18); laby[i][19] = new Case(b19);

    }

    public void createLaby1(){
        laby = new Case[20][20];
        //0
        createLine(0, true, false, true, true, true, true, true, true, true, true, 
        true, true, true, true, true, true, true, true, true, true);
        //1
        createLine(1, true, false, true, false, false, false, true, false, false, false, 
        false, false, true, false, false, false, false, false, false, false);
        //2
        createLine(2, true, false, true, false, true, true, true, false, true, true, 
        true, true, true, false, true, true, true, false, true, true);
        //3
        createLine(3, true, false, false, false, false, false, true, true, false, false, 
        false, false, false, false, false, false, true, false, false, false);
        //4
        createLine(4, true, true, true, true, true, false, true, true, true, false, 
        true, true, true, true, true, true, true, true, true, false);
        //5
        createLine(5, true, false, false, false, true, false, false, false, true, false, 
        true, false, false, false, false, false, true, false, true, false);
        //6
        createLine(6, true, false, true, true, true, false, true, false, true, false, 
        true, true, true, true, true, false, true, false, true, false); 
        //7
        createLine(7, true, false, true, false, false, false, true, false, true, false, 
        false, false, false, false, false, false, true, false, false, false);        
        //8
        createLine(8, true, false, true, false, true, true, true, false, true, true, 
        true, true, true, true, true, false, true, false, true, true);
        //9
        createLine(9, true, false, true, false, true, false, false, false, false, false, 
        false, false, true, false, true, false, true, false, true, false); 
        //10
        createLine(10, true, false, true, true, true, false, true, true, true, false, 
        true, false, true, false, true, false, true, false, true, false);
        //11
        createLine(11, true, false, false, false, false, false, true, false, false, false, 
        true, false, false, false, true, false, true, false, false, false);
        //12
        createLine(12, true, true, true, true, true, true, true, false, true, false, 
        true, true, true, true, true, false, true, true, true, false);
        //13
        createLine(13, true, false, false, false, false, false, true, false, true, false, 
        false, false, false, false, false, false, false, false, true, false);
        //14
        createLine(14, true, true, true, true, true, false, true, false, true, false, 
        true, true, true, true, true, true, true, true, true, false);
        //15
        createLine(15, true, false, false, false, false, false, true, false, true, false, 
        false, false, false, false, true, false, false, false, false, false);
        //16
        createLine(16, true, false, true, false, true, true, true, false, true, true, 
        true, true, true, true, true, false, true, true, true, true);
        //17
        createLine(17, true, false, true, false, false, false, false, false, false, false, 
        true, false, false, false, false, false, false, false, false, false);
        //18
        createLine(18, true, false, true, false, true, true, true, true, true, false, 
        true, true, true, false, true, true, true, true, true, true);
        //19
        createLine(19, true, false, true, false, true, false, false, false, false, false, 
        false, false, true, false, false, false, false, false, false, false);

    }

    /*
     * -----
     * Getters et Setters
     * -----
     */

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public Case[][] getLaby() {
        return laby;
    }

}