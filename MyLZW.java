/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int L_MAX = 65536;       // max number of codewords = 2^12
    private static int W = 9;               // codeword width
    private static boolean resetMode = false; // reset mode = true, do nothing mode = false
    private static boolean monitorMode = false; // monitor mode = true. reset/do nothing = false
    
    public static void compress() {
        // Compression ratio variables
        long bitsRead = 0;
        long bitsWrote = 0;
        double currentRatio = 0;
        double oldRatio = 0;
        // End compression ratio variables
        BinaryStdOut.write(resetMode); // First bit determines if it is in -R mode \\ 00 = do nothing, 10 = reset mode
        BinaryStdOut.write(monitorMode); // Second bit determines if it is in -M mode \\ 01 = monitor mode
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF -- equates to 257
        while (input.length() > 0) {
            if(st.size() == 512) W = 10; // added
            if(st.size() == 1024) W = 11; // added
            if(st.size() == 2048) W = 12; // added
            if(st.size() == 4096) W = 13;
            if(st.size() == 8192) W = 14;
            if(st.size() == 16384) W = 15;
            if(st.size() == 32768) W = 16;
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            if(st.get(s) == 256) System.err.println("256 SEEN AND ADDED WTF R U DOIN");
            //if(iteration > 65530 && iteration < 83775)
            //    System.err.println("Iteration " + iteration + ": codeword=" + st.get(s));
            int t = s.length(); // get length of symbol to be added
            if(monitorMode) { // at each iteration, add bitsRead and bitsWrote
                bitsRead += (long)t * 8;
                bitsWrote += (long)W;
                //System.err.println("Bits read: " + bitsRead + "\nBits wrote: " + bitsWrote);
            }
            if (t < input.length() && code < L) {    // Add s to symbol table.
                //if(((code<5000)&&(code>4096)))System.err.println("Added " + input.substring(0,t+1) + " to dictionary. Code: " + code + " | TST Size " + st.size()); // added
                st.put(input.substring(0, t + 1), code++);
                if(code == 512) L = 1024;
                if(code == 1024) L = 2048;
                if(code == 2048) L = 4096;
                if(code == 4096) L = 8192;
                if(code == 8192) L = 16384;
                if(code == 16384) L = 32788;
                if(code == 32768) L = 65536;
            } else if ((code == L && resetMode)) {// || forceReset) { // if we hit max size of dic.
                BinaryStdOut.write(st.get(s), W);      //added this
                W = 9; // set bit count back to 9
                L = 512; // set # of codewords back to 2^W
                st = new TST<Integer>(); // remake TST
                for (int i = 0; i < R; i++) // fill first 256 chars with chars
                    st.put("" + (char) i, i); // char[i]
                code = R+1;  // R is codeword for EOF -- equates to 257
            } if (code == L && monitorMode && oldRatio == (double)0) {
                oldRatio = (double)bitsRead/(double)bitsWrote;
                currentRatio = oldRatio;
            } else if (code == L && monitorMode && oldRatio > (double)0) {
                currentRatio = (double)bitsRead/(double)bitsWrote;
                if(oldRatio/currentRatio >= (double)1.1) {
                    oldRatio = 0.0;
                    currentRatio = 0.0;
                    bitsRead = 0;
                    bitsWrote = 0;
                    BinaryStdOut.write(st.get(s), W);      //** write LAST entry we're adding from previous dictionary
                    W = 9; // set bit count back to 9
                    L = 512; // set # of codewords back to 2^W
                    st = new TST<Integer>(); // remake TST
                    for (int i = 0; i < R; i++) // fill first 256 chars with chars
                        st.put("" + (char) i, i); // char[i]
                    code = R+1;  // R is codeword for EOF -- equates to 257
                }
            }
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    }


    public static void expand() {
        String[] st = new String[L_MAX];
        int i; // next available codeword value
        long bitsRead = 0;
        long bitsWrote = 0;
        double oldRatio = 0.0;
        double currentRatio = 0.0;
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF
        resetMode = BinaryStdIn.readBoolean(); // First bit determines if it is in -R mode \\ 00 = do nothing, 10 = reset mode
        monitorMode = BinaryStdIn.readBoolean(); // Second bit determines if it is in -M mode \\ 01 = monitor mode
        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
        while (true) {
            int preW = W;
            if(i == 512){ W = 10; L = 1024; }
            if(i == 1024){ W = 11; L = 2048; }
            if(i == 2048){ W = 12; L = 4096; }
            if(i == 4096){ W = 13; L = 8192; }
            if(i == 8192){ W = 14; L = 16384; }
            if(i == 16384){ W = 15; L = 32768; }
            if(i == 32768){ W = 16; L = 65536; }
            BinaryStdOut.write(val);
            int writeLength = val.length();
            String writeVal = val;
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L){
                st[i++] = val + s.charAt(0);
                val = s;
            } else if (i == L && monitorMode) {
                val = s;
            }
            if(monitorMode) { // at each iteration, add bitsRead and bitsWrote
                bitsRead += (long)preW; // read W bits
                bitsWrote += (long)writeLength*8; // wrote codeword-string-length bits
            }
            // added here
            if (i == L_MAX && resetMode) {
                codeword = BinaryStdIn.readInt(W); // one final 16-bit read
                if (codeword == R) return;           // expanded message is empty string
                val = s;
                //System.err.println(val);
                BinaryStdOut.write(val);
                W = 9; // set W back to size 9
                L = 512; // set L back to size 512 (2^W)
                st = new String[L_MAX];
                for (i = 0; i < R; i++)
                    st[i] = "" + (char) i;
                st[i++] = "";
                codeword = BinaryStdIn.readInt(W);
                val = st[codeword];
            } else if (i == L_MAX && monitorMode && oldRatio == (double)0) {
                oldRatio = (double)bitsWrote/(double)bitsRead; // backwards now because decompression ratio
                currentRatio = oldRatio;
            } else if (i == L_MAX && monitorMode && oldRatio > (double)0) {
                currentRatio = (double)bitsWrote/(double)bitsRead; // backwards
                if(oldRatio/currentRatio >= (double)1.1) {
                    oldRatio = 0.0;
                    currentRatio = 0.0;
                    bitsRead = 0;
                    bitsWrote = 0;
                    //codeword = BinaryStdIn.readInt(W); // one final 16-bit read
                    //val = st[codeword];
                    if (codeword == R) return;
                    //BinaryStdOut.write(val);
                    W = 9; // set W back to size 9
                    L = 512; // set L back to size 512 (2^W)
                    st = new String[L_MAX];
                    for (i = 0; i < R; i++)
                        st[i] = "" + (char) i;
                    st[i++] = "";
                    codeword = BinaryStdIn.readInt(W);
                    val = st[codeword];
                }
            }
            // end of add
            if(!monitorMode && !resetMode)val = s;//was cmtd out
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        if      (args[0].equals("-")) {
            //compress(); // remove this after done with monitor mode
            /**/ // args checking
            if (args.length != 2) {
                System.err.println("Illegal command line argument.\nRun compression via\n\tjava MyLZW - -mode <input_file> output_file");
                return;
            }
            if  (args[1].equalsIgnoreCase("-r")) {
                resetMode = true;
                compress();
            } else if (args[1].equalsIgnoreCase("-m")) {
                monitorMode = true;
                compress();
            } else if (args[1].equalsIgnoreCase("-n")) {
                compress(); 
            } else {
                System.err.println("Unknown compression flag. After compression argument, "     +
                                    "add the following:\n\t\"-n\": Do nothing mode\n\t\"-r\": " +
                                    "Reset mode\n\t\"-m\": Monitor mode");
                return;
            }
            /**/
        }
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
