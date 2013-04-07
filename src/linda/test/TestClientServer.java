/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package linda.test;

import java.util.Collection;
import linda.*;

/**
 *
 * @author Alexandra Jacquet
 * @author Florian Vetu
 */
public class TestClientServer {

    /**
     * MyCallBack
     */
    private static class MyCallback11 implements Callback {

        @Override
        public void call(Tuple t) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
            }
            System.out.println("Got in test 11" + t);

        }
    }

    private static class MyCallback12 implements Callback {

        @Override
        public void call(Tuple t) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println("Got in test 12 " + t);

        }
    }

    private static class MyCallback implements Callback {

        @Override
        public void call(Tuple t) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println("Got " + t);
        }
    }

    public static void main(String[] a) {

        final Linda linda = new linda.server.LindaClient("rmi://127.0.0.1:8080/linda");
        final Linda linda2 = new linda.server.LindaClient("rmi://127.0.0.1:8080/linda");

        // test take non existant 
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // ---------- test 1 ----------- 
                System.out.println("---------- test1 take ----------");
                Tuple motif = new Tuple(Integer.class, String.class);
                Tuple res1 = linda.take(motif);
                // affichage enregistrement du take
                // take bloquant

                // réalisation des write dans un autre thread

                // débloqué
                System.out.println("test1 take OK ");
                System.out.println("(1) Resultat:" + res1);
                linda.debug("(1)");

                // ---------- test 2 ----------- 
                System.out.println("---------- test2 read ----------");
                // realisation d'un read bloquant
                Tuple res2 = linda.read(motif);
                // débloqué
                System.out.println("test2 read OK ");
                System.out.println("(1) Resultat:" + res2);
                linda.debug("(2)");

                // ajout de plusieurs tuples
                Tuple t3 = new Tuple(4, 6);
                System.out.println("(4) write: " + t3);
                linda.write(t3);
                System.out.println("(5) write: " + t3);
                linda.write(t3);
                System.out.println("(6) write: " + t3);
                linda.write(t3);

                Tuple t4 = new Tuple(4, "test3");
                System.out.println("(7) write: " + t4);
                linda.write(t4);

                Tuple t5 = new Tuple(4, 7);
                System.out.println("(8) write: " + t5);
                linda.write(t5);

                Tuple t6 = new Tuple(4, "test7");
                System.out.println("(9) write: " + t6);
                linda.write(t6);

                linda.debug("(3)");

                // ------------ test 3 -----------------
                System.out.println("---------- test3 tryTake ----------");
                // réalisation d'un tryTake qui renvoi null
                Tuple motif2 = new Tuple(String.class, Integer.class);
                Tuple res3 = linda.tryTake(motif2);
                if (res3 == null) {
                    System.out.println("test3 tryTake OK : try to take : " + motif2.toString());
                } else {
                    System.out.println("test3 tryTake failed ");
                }
                linda.debug("(4)");
                // ------------ test 4 -----------------
                System.out.println("---------- test4 tryTake ----------");
                // réalisation d'un tryTake qui renvoi un tuple
                Tuple res4 = linda.tryTake(motif);
                if (res4 == null) {
                    System.out.println("test4 tryTake failed ");
                } else {
                    System.out.println("test4 tryTake OK : try to take : " + motif.toString());
                }
                System.out.println("(4) Resultat:" + res4);
                linda.debug("(5)");

                // ------------ test 5 -----------------
                System.out.println("---------- test5 tryRead ----------");
                // réalisation d'un tryRead qui renvoi un tuple
                Tuple res5 = linda.tryRead(motif);
                if (res5 == null) {
                    System.out.println("test5 tryRead failed ");
                } else {
                    System.out.println("test5 tryRead OK : try to read : " + motif.toString());
                }
                System.out.println("(5) Resultat:" + res5);
                linda.debug("(6)");

                // ------------ test 6 -----------------
                System.out.println("---------- test6 tryRead ----------");
                // réalisation d'un tryRead qui renvoi un tuple
                Tuple res6 = linda.tryRead(motif2);
                if (res6 == null) {
                    System.out.println("test6 tryRead OK : try to read : " + motif2.toString());
                } else {
                    System.out.println("test6 tryRead failed ");
                }
                linda.debug("(7)");

                // ------------ test 7 -----------------
                System.out.println("---------- test7 ReadAll ----------");
                // réalisation d'un readAll
                Collection<Tuple> res7 = linda.readAll(motif);
                System.out.println("Resultat read all motif : " + motif.toString() + " -> " + res7.toString());
                System.out.println("test7 readAll OK ");
                linda.debug("(8)");

                // ------------ test 8 -----------------
                System.out.println("---------- test8 TakeAll ----------");
                // réalisation d'un takeAll
                Tuple motif3 = new Tuple(Integer.class, Integer.class);
                Collection<Tuple> res8 = linda.takeAll(motif3);
                System.out.println("Resultat take all motif : " + motif3.toString() + " -> " + res8.toString());
                System.out.println("test8 takeAll OK ");
                linda.debug("(8)");


                // ------------ test 9 -----------------
                System.out.println("---------- test9 TakeAll vide ----------");
                // réalisation d'un takeAll
                Collection<Tuple> res9 = linda.takeAll(motif3);
                System.out.println("Resultat take all motif : " + motif3.toString() + " -> " + res9.toString());
                System.out.println("test9 takeAll vide OK ");
                linda.debug("(9)");

                // ------------ test 10 -----------------
                System.out.println("---------- test10 ReadAll vide ----------");
                // réalisation d'un readAll
                Collection<Tuple> res10 = linda.readAll(motif3);
                System.out.println("Resultat read all motif : " + motif3.toString() + " -> " + res10.toString());
                System.out.println("test10 readAll vide OK ");
                linda.debug("(10)");

                // ------------ test 11 -----------------
                System.out.println("---------- test11 eventRegister  - take immediat possible ----------");
                // immediat qui marche
                linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, motif, new AsynchronousCallback(new MyCallback11()));
                System.out.println("test11 take immediat possible : test OK ");
                linda.debug("(11)");

                // ------------ test 12 -----------------
                System.out.println("---------- test12 eventRegister  - take immediat pas possible ----------");
                // immediat qui ne marche pas et passe en future
                linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, motif2, new AsynchronousCallback(new MyCallback12()));
                System.out.println("test12 en attente ajout motif template : " + motif2.toString());

                Tuple t7 = new Tuple("test12", 1);
                System.out.println("(7) write: " + t7);
                linda.write(t7);

                linda.debug("(12)");

                System.out.println("test12 take immediat passé en future : test OK ");
                
                //on enlève le tuple restant pour vider la mémoire
                Tuple res13 = linda.take(motif);
                
                //----------- test13 ---------------------------
                 System.out.println("---------- test13 client1 : take bloquant / client2 : write qui débloque le client1 ----------");
                 System.out.println("attente du client1 ");
                 Tuple res14 = linda.take(motif); 
                // affichage enregistrement du take
                // take bloquant

                // réalisation des write par un deuxieme client dans un autre thread

                // débloqué
                System.out.println("(13) Resultat du take client1 :" + res14);
                System.out.println("test13 take OK ");
                linda.debug("(13)");


            }
        }.start();

        //write d'un tuple attendu pour un take
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(4, 5);
                System.out.println("(1) write: " + t1);
                linda.write(t1);

                Tuple t2 = new Tuple(4, "test1");
                System.out.println("(2) write: " + t2);
                linda.write(t2);
                // le deuxieme tuple est bien pris et enlevé de la mémoire

            }
        }.start();

        //write d'un tuple attendu pour le read
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t3 = new Tuple(4, "test2");
                linda.write(t3);
                System.out.println("(3) write: " + t3);

            }
        }.start();
        
         //write d'un tuple par le deuxieme client attendu pour le take
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t13 = new Tuple(13, "test13");
                linda2.write(t13);
                System.out.println("(13) write client2: " + t13);

            }
        }.start();

    }
}
