/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package linda.test;

/**
 *
 * @author Zanatoshi
 */
public class TestTransition {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final int [] val = {1,2,1,0};
        final int N = 4;
        final int V = 3;
        for(int i =0; i<N; i++)
        {
            final int j = i;
            new Thread() {
                public void run()
                {
                    System.out.println("J = " + j);
                    while(true)
                    {
                        synchronized(val)
                        {
                            if(j==0)
                            {
                                if(val[j] == val[N-1])
                                {
                                    val[j] = (val[j]+1)%V;
                                }
                            }
                            else
                            {
                                if(val[j] != val[j-1])
                                {
                                    val[j] = val[j-1];
                                }
                            }
                            for(int k : val)
                            {
                                System.out.print(k + " : ");
                            }
                            System.out.println();
                        }
                    }
                }
            }.start();
        }
    }
}
